package com.layor.tinyflow.service;

import com.layor.tinyflow.config.RabbitMQConfig;
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

    @Value("${clicks.mode:mq}")
    private String counterMode;

    @Value("${events.sampleRate:0.0}")
    private double sampleRate;

    private final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.atomic.AtomicLong> localTotal = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.atomic.AtomicLong> localDay = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 记录点击事件（使用消息队列）
     * 优先级：MQ > Local > Redis
     */
    @Async
    public void recordClick(String shortCode) {
        // 模式1：消息队列模式（推荐，高可靠）
        if ("mq".equalsIgnoreCase(counterMode)) {
            ClickMessage message = ClickMessage.builder()
                    .shortCode(shortCode)
                    .timestamp(System.currentTimeMillis())
                    .date(LocalDate.now().toString())
                    .build();
            try {
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CLICK_EXCHANGE, 
                    RabbitMQConfig.CLICK_ROUTING_KEY, 
                    message
                );
                log.debug("[MQ] Click message sent: {}", shortCode);
            } catch (Exception e) {
                log.error("[MQ ERROR] Failed to send click message for {}: {}", shortCode, e.getMessage());
                // 降级到本地模式
                localTotal.computeIfAbsent(shortCode, k -> new java.util.concurrent.atomic.AtomicLong()).incrementAndGet();
                localDay.computeIfAbsent(shortCode, k -> new java.util.concurrent.atomic.AtomicLong()).incrementAndGet();
            }
            return;
        }
        
        // 模式2：本地内存模式
        if ("local".equalsIgnoreCase(counterMode)) {
            localTotal.computeIfAbsent(shortCode, k -> new java.util.concurrent.atomic.AtomicLong()).incrementAndGet();
            localDay.computeIfAbsent(shortCode, k -> new java.util.concurrent.atomic.AtomicLong()).incrementAndGet();
            return;
        }
        
        // 模式3：Redis Pipeline 模式
        String totalKey = "tf:clicks:total";
        String dayKey = "tf:clicks:day:" + LocalDate.now();
        try {
            // 使用Pipeline减少网络往返
            redisTemplate.executePipelined(new org.springframework.data.redis.core.SessionCallback<Object>() {
                @Override
                public Object execute(org.springframework.data.redis.core.RedisOperations operations) {
                    operations.opsForHash().increment(dayKey, shortCode, 1);
                    operations.opsForHash().increment(totalKey, shortCode, 1);
                    return null;
                }
            });
        } catch (org.springframework.data.redis.RedisConnectionFailureException ex) {
            log.error("redis connect failed: {}", ex.getMessage());
        }
    }

    @Async
    public void recordClickEvent(String shortCode, String referer, String ua, String ip, String host, String device) {
        if (sampleRate <= 0.0) return;
        if (sampleRate < 1.0) {
            if (java.util.concurrent.ThreadLocalRandom.current().nextDouble() >= sampleRate) return;
        }
//        封装
        com.layor.tinyflow.entity.ClickEvent ev = com.layor.tinyflow.entity.ClickEvent.builder()
                .shortCode(shortCode)
                .ts(java.time.LocalDateTime.now())
                .referer(referer)
                .ua(ua)
                .ip(ip)
                .sourceHost(host)
                .deviceType(device)
                .city("")
                .country("")
                .build();
//        保存
        clickEventRepository.save(ev);
    }
    

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void flushCounters() {
        if ("local".equalsIgnoreCase(counterMode)) {
            for (var e : localTotal.entrySet()) {
                String code = e.getKey();
                long delta = e.getValue().getAndSet(0);
                if (delta > 0) { shortUrlRepository.incrementClickCountBy(code, delta); }
            }
            for (var e : localDay.entrySet()) {
                String code = e.getKey();
                long delta = e.getValue().getAndSet(0);
                if (delta > 0) { dailyClickRepo.incrementClickBy(code, delta); }
            }
            return;
        }
        String totalKey = "tf:clicks:total";
        String dayKey = "tf:clicks:day:" + LocalDate.now();
        String tmpTotal = totalKey + ":flush:" + System.currentTimeMillis();
        String tmpDay = dayKey + ":flush:" + System.currentTimeMillis();
        
        // 使用Pipeline批量重命名和读取，减少网络往返
        try {
            redisTemplate.executePipelined(new org.springframework.data.redis.core.SessionCallback<Object>() {
                @Override
                public Object execute(org.springframework.data.redis.core.RedisOperations operations) {
                    try { operations.rename(totalKey, tmpTotal); } catch (Exception ignored) {}
                    try { operations.rename(dayKey, tmpDay); } catch (Exception ignored) {}
                    return null;
                }
            });
        } catch (Exception ignored) {}
        
        java.util.Map<Object,Object> total = java.util.Collections.emptyMap();
        java.util.Map<Object,Object> day = java.util.Collections.emptyMap();
        try { total = redisTemplate.<Object,Object>opsForHash().entries(tmpTotal); } catch (Exception ignored) {}
        try { day = redisTemplate.<Object,Object>opsForHash().entries(tmpDay); } catch (Exception ignored) {}
        
        for (var e : total.entrySet()) {
            String code = String.valueOf(e.getKey());
            long delta = toLong(e.getValue());
            if (delta > 0) { shortUrlRepository.incrementClickCountBy(code, delta); }
        }
        for (var e : day.entrySet()) {
            String code = String.valueOf(e.getKey());
            long delta = toLong(e.getValue());
            if (delta > 0) { dailyClickRepo.incrementClickBy(code, delta); }
        }
        
        // 使用Pipeline批量删除临时key
        try {
            redisTemplate.executePipelined(new org.springframework.data.redis.core.SessionCallback<Object>() {
                @Override
                public Object execute(org.springframework.data.redis.core.RedisOperations operations) {
                    try { operations.delete(tmpTotal); } catch (Exception ignored) {}
                    try { operations.delete(tmpDay); } catch (Exception ignored) {}
                    return null;
                }
            });
        } catch (Exception ignored) {}
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        try { return Long.parseLong(String.valueOf(value)); } catch (Exception ex) { return 0L; }
    }
}
