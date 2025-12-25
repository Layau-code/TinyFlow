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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
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
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired(required = false)
    private IpLocationService ipLocationService;

    @Value("${clicks.mode:mq}")
    private String counterMode;

    @Value("${events.sampleRate:0.0}")
    private double sampleRate;

    private final ConcurrentHashMap<String, AtomicLong> localTotal = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> localDay = new ConcurrentHashMap<>();
    
    // 详细事件批量缓冲队列
    private final ConcurrentLinkedQueue<ClickEvent> eventBuffer = new ConcurrentLinkedQueue<>();
    private static final int EVENT_BATCH_SIZE = 100;

    /**
     * 记录点击事件
     * 优先级：Local > Redis Snapshot > MQ
     * 
     * 方案 C：本地内存 + Redis 快照 + 定期持久化
     * ├─ 第一阶段（10s）：本地计数 → Redis 快照（冷备份）
     * └─ 第二阶段（60s）：Redis 快照 → 数据库（正式记账）
     */
    @Async
    public void recordClick(String shortCode) {
        // 所有模式都先写本地内存（最快）
        localTotal.computeIfAbsent(shortCode, k -> new AtomicLong())
                  .incrementAndGet();
        localDay.computeIfAbsent(shortCode, k -> new AtomicLong())
                .incrementAndGet();
        
        log.debug("[LOCAL] Click recorded: {}", shortCode);
    }

    /**
     * 记录详细点击事件（包含 IP 地理位置解析）
     * 使用批量缓冲队列，定时批量写入数据库
     */
    @Async
    public void recordClickEvent(String shortCode, String referer, String ua, String ip, String host, String device) {
        if (sampleRate <= 0.0) return;
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
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void flushEventBuffer() {
        if (eventBuffer.isEmpty()) return;
        
        long startTime = System.currentTimeMillis();
        List<ClickEvent> batch = new ArrayList<>();
        
        // 每次最多处理 500 条
        int count = 0;
        while (!eventBuffer.isEmpty() && count < 500) {
            ClickEvent ev = eventBuffer.poll();
            if (ev != null) {
                batch.add(ev);
                count++;
            }
        }
        
        if (!batch.isEmpty()) {
            try {
                clickEventRepository.saveAll(batch);
                long duration = System.currentTimeMillis() - startTime;
                log.info("[EVENT FLUSH] Saved {} click events to DB, duration={}ms", batch.size(), duration);
            } catch (Exception e) {
                log.error("[EVENT FLUSH ERROR] Failed to save click events: {}", e.getMessage(), e);
                // 失败时重新放回队列
                eventBuffer.addAll(batch);
            }
        }
    }

    /**
     * 第一阶段：定期快照到 Redis（10 秒一次）
     * 作用：冷备份，防止服务宕机时丢失数据
     */
    @Scheduled(fixedDelay = 10000)  // 每 10 秒
    public void snapshotToRedis() {
        try {
            long startTime = System.currentTimeMillis();
            int snapshotCount = 0;
            
            // 快照总点击数
            for (var entry : localTotal.entrySet()) {
                String code = entry.getKey();
                long count = entry.getValue().get();  // 只读，不重置
                if (count > 0) {
                    redisTemplate.opsForHash().put("tf:click:snapshot:total", code, String.valueOf(count));
                    snapshotCount++;
                }
            }
            
            // 快照每日点击数
            for (var entry : localDay.entrySet()) {
                String code = entry.getKey();
                long count = entry.getValue().get();  // 只读，不重置
                if (count > 0) {
                    String dayKey = "tf:click:snapshot:day:" + LocalDate.now();
                    redisTemplate.opsForHash().put(dayKey, code, String.valueOf(count));
                    snapshotCount++;
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            if (snapshotCount > 0) {
                log.info("[SNAPSHOT] Saved {} entries to Redis, duration={}ms", snapshotCount, duration);
            }
        } catch (Exception e) {
            log.error("[SNAPSHOT ERROR] Failed to snapshot to Redis: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 第二阶段：定期持久化到数据库（60 秒一次）
     * 作用：正式记账，将快照中的数据写入数据库
     */
    @Scheduled(fixedDelay = 60000)  // 每 60 秒
    @Transactional
    public void syncFromRedisToDB() {
        try {
            long startTime = System.currentTimeMillis();
            int totalUpdates = 0;
            int dailyUpdates = 0;
            
            // 读取总点击快照并刷库
            java.util.Map<Object, Object> totalSnapshot = redisTemplate.opsForHash()
                    .entries("tf:click:snapshot:total");
            for (var entry : totalSnapshot.entrySet()) {
                String code = String.valueOf(entry.getKey());
                long count = toLong(entry.getValue());
                if (count > 0) {
                    shortUrlRepository.incrementClickCountBy(code, count);
                    totalUpdates++;
                }
            }
            
            // 读取每日点击快照并刷库
            String daySnapshotKey = "tf:click:snapshot:day:" + LocalDate.now();
            java.util.Map<Object, Object> daySnapshot = redisTemplate.opsForHash()
                    .entries(daySnapshotKey);
            for (var entry : daySnapshot.entrySet()) {
                String code = String.valueOf(entry.getKey());
                long count = toLong(entry.getValue());
                if (count > 0) {
                    dailyClickRepo.incrementClickBy(code, count);
                    dailyUpdates++;
                }
            }
            
            // 刷库成功后，清空 Redis 快照（只清快照，本地内存继续累加）
            redisTemplate.delete("tf:click:snapshot:total");
            redisTemplate.delete(daySnapshotKey);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("[SYNC] Persisted {} total clicks and {} daily clicks to DB, duration={}ms", 
                    totalUpdates, dailyUpdates, duration);
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