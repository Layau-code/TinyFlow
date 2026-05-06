package com.layor.tinyflow.service;

import com.layor.tinyflow.config.RabbitMQConfig;
import com.layor.tinyflow.entity.ClickEvent;
import com.layor.tinyflow.entity.ClickMessage;
import com.layor.tinyflow.entity.DailyClick;
import com.layor.tinyflow.repository.DailyClickRepository;
import com.layor.tinyflow.repository.ShortUrlRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class ClickRecorderService {

    @Autowired
    private DailyClickRepository dailyClickRepo;
    @Autowired
    private ShortUrlRepository shortUrlRepository;
    @Autowired
    private com.layor.tinyflow.repository.ClickEventRepository clickEventRepository;
    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;
    @Autowired(required = false)
    private IpLocationService ipLocationService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${clicks.mode:mq}")
    private String counterMode;

    @Value("${events.sampleRate:0.0}")
    private double sampleRate;
    
    @Value("${events.buffer.maxSize:10000}")
    private int maxBufferSize;
    
    @Value("${events.flush.minBatchSize:100}")
    private int minBatchSize;
    
    @Value("${events.flush.maxBatchSize:1000}")
    private int maxBatchSize;
    
    @Value("${events.flush.strategy:jdbc}")
    private String flushStrategy;  // jdbc, jpa, auto
    
    // 详细事件批量缓冲队列
    private final ConcurrentLinkedQueue<ClickEvent> eventBuffer = new ConcurrentLinkedQueue<>();
    private static final int EVENT_BATCH_SIZE = 100;
    
    // 监控指标
    private final AtomicLong totalEventsProcessed = new AtomicLong(0);
    private final AtomicLong totalJdbcBatchSuccess = new AtomicLong(0);
    private final AtomicLong totalJdbcBatchFailed = new AtomicLong(0);
    private final AtomicLong totalJpaFallbackSuccess = new AtomicLong(0);
    private final AtomicLong totalJpaFallbackFailed = new AtomicLong(0);
    private volatile long lastFlushTimestamp = System.currentTimeMillis();
    private volatile long lastFlushDuration = 0L;

    /**
     * 记录点击事件（MQ模式）
     * 发送MQ消息，由 ClickCountAggregator 消费聚合
     * 
     * 性能：
     * - 延迟：1-2ms（发送MQ）
     * - 可见性延迟：~1秒（聚合窗口）
     * - QPS：5万+
     */
    @Async
    public void recordClick(String shortCode) {
        if (rabbitTemplate == null) {
            log.warn("[MQ] RabbitTemplate not available, click not recorded: {}", shortCode);
            return;
        }
        
        try {
            ClickMessage msg = ClickMessage.builder()
                    .shortCode(shortCode)
                    .timestamp(System.currentTimeMillis())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.CLICK_EXCHANGE, RabbitMQConfig.CLICK_ROUTING_KEY, msg);
            log.debug("[MQ] Click message sent: {}", shortCode);
        } catch (Exception e) {
            log.error("[MQ ERROR] Failed to send click message for {}: {}", shortCode, e.getMessage());
        }
    }

    /**
     * 记录详细点击事件（包含 IP 地理位置解析）
     * 使用批量缓冲队列，定时批量写入数据库
     */
    @Async
    public void recordClickEvent(String shortCode, String referer, String ua, String ip, String host, String device) {
        // 采样率控制：如果采样率小于等于0，则不记录任何事件
        if (sampleRate <= 0.0) return;

        // 如果采样率小于1.0，则根据随机数决定是否采样当前事件
        if (sampleRate < 1.0) {
            if (ThreadLocalRandom.current().nextDouble() >= sampleRate) return;
        }
        
        // IP 地理位置解析
        String city = "";
        String country = "";
        if (ipLocationService != null && ip != null && !ip.isEmpty()) {
            try {
                city = ipLocationService.getCity(ip);
                country = ipLocationService.getCountry(ip);
            } catch (Exception e) {
                log.debug("[EVENT] IP location parse failed for {}: {}", ip, e.getMessage());
            }
        }
        
        // 构建事件对象
        ClickEvent ev = ClickEvent.builder()
                .shortCode(shortCode)
                .ts(LocalDateTime.now())
                .referer(referer)
                .ua(ua)
                .ip(ip)
                .sourceHost(host)
                .deviceType(device)
                .city(city)
                .country(country)
                .build();
        
        // 加入批量缓冲队列
        eventBuffer.offer(ev);
        
        // 缓冲区达到阈值时触发批量写入
        if (eventBuffer.size() >= EVENT_BATCH_SIZE) {
            flushEventBuffer();
        }
        
        log.debug("[EVENT] Buffered click event: code={}, city={}, country={}", shortCode, city, country);
    }
    
    /**
     * 定时刷新事件缓冲区（5秒一次）
     * 方案 D：异步 + 降级策略
     * - 主路径：JDBC 批量插入（高性能）
     * - 备用路径：JPA saveAll（保证可靠性）
     * - 动态批量：根据队列压力自适应调整
     * - 监控告警：实时跟踪性能指标
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void flushEventBuffer() {
        if (eventBuffer.isEmpty()) return;
        
        long startTime = System.currentTimeMillis();
        
        // 动态计算批量大小
        int batchSize = calculateDynamicBatchSize();
        List<ClickEvent> batch = collectBatch(batchSize);
        
        if (!batch.isEmpty()) {
            boolean success = flushWithFallback(batch, startTime);
            
            // 更新监控指标
            lastFlushTimestamp = System.currentTimeMillis();
            lastFlushDuration = lastFlushTimestamp - startTime;
            totalEventsProcessed.addAndGet(batch.size());
            
            // 检查告警条件
            checkAndAlert();
        }
    }
    
    /**
     * 动态计算批量大小（根据队列压力自适应）
     */
    private int calculateDynamicBatchSize() {
        int queueSize = eventBuffer.size();
        
        // 队列积压严重（超过80%），使用最大批量加速处理
        if (queueSize > maxBufferSize * 0.8) {
            log.warn("[BATCH] Queue is {}% full, using max batch size {}", 
                    (queueSize * 100 / maxBufferSize), maxBatchSize);
            return maxBatchSize;
        }
        
        // 队列压力中等（50%-80%），使用中等批量
        if (queueSize > maxBufferSize * 0.5) {
            int mediumBatch = (minBatchSize + maxBatchSize) / 2;
            return mediumBatch;
        }
        
        // 队列压力小（20%-50%），使用标准批量
        if (queueSize > maxBufferSize * 0.2) {
            return minBatchSize * 2;
        }
        
        // 队列压力很小（<20%），使用最小批量
        return minBatchSize;
    }
    
    /**
     * 从队列中收集指定数量的事件
     */
    private List<ClickEvent> collectBatch(int batchSize) {
        List<ClickEvent> batch = new ArrayList<>(batchSize);
        int count = 0;
        
        while (!eventBuffer.isEmpty() && count < batchSize) {
            ClickEvent ev = eventBuffer.poll();
            if (ev != null) {
                batch.add(ev);
                count++;
            }
        }
        
        return batch;
    }
    
    /**
     * 带降级策略的刷新方法
     * 主路径：JDBC 批量插入
     * 备用路径：JPA saveAll
     */
    private boolean flushWithFallback(List<ClickEvent> batch, long startTime) {
        // 策略1：自动选择（根据历史成功率）
        if ("auto".equals(flushStrategy)) {
            long jdbcSuccess = totalJdbcBatchSuccess.get();
            long jdbcFailed = totalJdbcBatchFailed.get();
            long totalJdbc = jdbcSuccess + jdbcFailed;
            
            // 如果 JDBC 失败率超过 20%，直接使用 JPA
            if (totalJdbc > 10 && (jdbcFailed * 100 / totalJdbc) > 20) {
                log.warn("[AUTO] JDBC failure rate is high, using JPA directly");
                return flushWithJpa(batch, startTime);
            }
        }
        
        // 策略2：强制使用 JPA
        if ("jpa".equals(flushStrategy)) {
            return flushWithJpa(batch, startTime);
        }
        
        // 策略3：优先使用 JDBC（默认）
        try {
            flushWithJdbc(batch, startTime);
            totalJdbcBatchSuccess.incrementAndGet();
            return true;
        } catch (Exception e) {
            log.warn("[JDBC FAILED] Batch insert failed, falling back to JPA: {}", e.getMessage());
            totalJdbcBatchFailed.incrementAndGet();
            
            // 降级到 JPA
            return flushWithJpa(batch, startTime);
        }
    }
    
    /**
     * JDBC 批量插入（主路径，高性能）
     */
    private void flushWithJdbc(List<ClickEvent> batch, long startTime) {
        String sql = "INSERT INTO click_event " +
                     "(short_code, ts, referer, ua, ip, source_host, device_type, city, country) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ClickEvent ev = batch.get(i);
                ps.setString(1, ev.getShortCode());
                ps.setTimestamp(2, ev.getTs() != null ? Timestamp.valueOf(ev.getTs()) : null);
                ps.setString(3, ev.getReferer());
                ps.setString(4, ev.getUa());
                ps.setString(5, ev.getIp());
                ps.setString(6, ev.getSourceHost());
                ps.setString(7, ev.getDeviceType());
                ps.setString(8, ev.getCity());
                ps.setString(9, ev.getCountry());
            }
            
            @Override
            public int getBatchSize() {
                return batch.size();
            }
        });
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("[JDBC BATCH] Inserted {} events, duration={}ms, qps={}", 
                batch.size(), duration, batch.size() * 1000 / Math.max(duration, 1));
    }
    
    /**
     * JPA 批量插入（备用路径，可靠性优先）
     */
    private boolean flushWithJpa(List<ClickEvent> batch, long startTime) {
        try {
            clickEventRepository.saveAll(batch);
            long duration = System.currentTimeMillis() - startTime;
            log.info("[JPA FALLBACK] Saved {} events, duration={}ms", batch.size(), duration);
            totalJpaFallbackSuccess.incrementAndGet();
            return true;
        } catch (Exception e) {
            log.error("[JPA FAILED] Failed to save click events: {}", e.getMessage(), e);
            totalJpaFallbackFailed.incrementAndGet();
            
            // 两种方法都失败，重新放回队列
            if (eventBuffer.size() < maxBufferSize) {
                eventBuffer.addAll(batch);
                log.warn("[REQUEUE] Re-queued {} events, queue size={}", batch.size(), eventBuffer.size());
            } else {
                log.error("[DISCARD] Queue is full, discarding {} events", batch.size());
            }
            return false;
        }
    }
    
    /**
     * 监控告警检查
     */
    private void checkAndAlert() {
        int queueSize = eventBuffer.size();
        
        // 告警1：队列即将满
        if (queueSize > maxBufferSize * 0.9) {
            log.error("[ALERT] Event buffer is {}% full ({}/{}), risk of data loss!", 
                    (queueSize * 100 / maxBufferSize), queueSize, maxBufferSize);
        } else if (queueSize > maxBufferSize * 0.8) {
            log.warn("[WARNING] Event buffer is {}% full ({}/{})", 
                    (queueSize * 100 / maxBufferSize), queueSize, maxBufferSize);
        }
        
        // 告警2：处理延迟过高
        if (lastFlushDuration > 10000) {
            log.error("[ALERT] Flush duration is too high: {}ms, check database performance!", 
                    lastFlushDuration);
        } else if (lastFlushDuration > 5000) {
            log.warn("[WARNING] Flush duration is high: {}ms", lastFlushDuration);
        }
        
        // 告警3：失败率过高
        long totalAttempts = totalJdbcBatchSuccess.get() + totalJdbcBatchFailed.get() + 
                            totalJpaFallbackSuccess.get() + totalJpaFallbackFailed.get();
        long totalFailed = totalJdbcBatchFailed.get() + totalJpaFallbackFailed.get();
        
        if (totalAttempts > 10 && (totalFailed * 100 / totalAttempts) > 10) {
            log.error("[ALERT] Failure rate is {}%, check system health!", 
                    (totalFailed * 100 / totalAttempts));
        }
    }
    
    /**
     * 获取监控指标（用于监控端点）
     */
    public String getMetrics() {
        return String.format(
            "ClickRecorder Metrics:\n" +
            "  Queue Size: %d/%d (%.1f%%)\n" +
            "  Total Processed: %d\n" +
            "  JDBC Success: %d\n" +
            "  JDBC Failed: %d\n" +
            "  JPA Fallback Success: %d\n" +
            "  JPA Fallback Failed: %d\n" +
            "  Last Flush Duration: %dms\n" +
            "  Last Flush Time: %d\n",
            eventBuffer.size(), maxBufferSize, (eventBuffer.size() * 100.0 / maxBufferSize),
            totalEventsProcessed.get(),
            totalJdbcBatchSuccess.get(),
            totalJdbcBatchFailed.get(),
            totalJpaFallbackSuccess.get(),
            totalJpaFallbackFailed.get(),
            lastFlushDuration,
            lastFlushTimestamp
        );
    }


    
    /**
     * 定期从Redis持久化到数据库（60秒一次）
     * 作用：将Redis中的计数批量更新到数据库
     */
    @Scheduled(fixedDelay = 60000)  // 每 60 秒
    @Transactional
    public void syncFromRedisToDB() {
        try {
            long startTime = System.currentTimeMillis();
            int totalUpdates = 0;
            int dailyUpdates = 0;
            
            // 读取总点击数并刷库
            java.util.Set<String> totalKeys = redisTemplate.keys("click:total:*");
            if (totalKeys != null && !totalKeys.isEmpty()) {
                for (String key : totalKeys) {
                    String code = key.replace("click:total:", "");
                    String countStr = redisTemplate.opsForValue().get(key);
                    long count = toLong(countStr);
                    
                    if (count > 0) {
                        shortUrlRepository.incrementClickCountBy(code, count);
                        totalUpdates++;
                        // 刷库成功后删除Redis键
                        redisTemplate.delete(key);
                    }
                }
            }
            
            // 读取每日点击数并刷库
            java.util.Set<String> dailyKeys = redisTemplate.keys("click:daily:*");
            if (dailyKeys != null && !dailyKeys.isEmpty()) {
                for (String key : dailyKeys) {
                    String code = key.replace("click:daily:", "");
                    String countStr = redisTemplate.opsForValue().get(key);
                    long count = toLong(countStr);
                    
                    if (count > 0) {
                        dailyClickRepo.incrementClickBy(code, count);
                        dailyUpdates++;
                        // 刷库成功后删除Redis键
                        redisTemplate.delete(key);
                    }
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            if (totalUpdates > 0 || dailyUpdates > 0) {
                log.info("[SYNC] Persisted {} total clicks and {} daily clicks to DB, duration={}ms", 
                        totalUpdates, dailyUpdates, duration);
            }
        } catch (Exception e) {
            log.error("[SYNC ERROR] Failed to sync from Redis to DB: {}", e.getMessage(), e);
        }
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        try { return Long.parseLong(String.valueOf(value)); } catch (Exception ex) { return 0L; }
    }
}