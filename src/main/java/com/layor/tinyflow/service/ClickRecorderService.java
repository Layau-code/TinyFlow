package com.layor.tinyflow.service;

import com.layor.tinyflow.entity.DailyClick;
import com.layor.tinyflow.repository.DailyClickRepository;
import com.layor.tinyflow.repository.ShortUrlRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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

    @Value("${clicks.mode:redis}")
    private String counterMode;

    @Value("${events.sampleRate:0.0}")
    private double sampleRate;

    private final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.atomic.AtomicLong> localTotal = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.atomic.AtomicLong> localDay = new java.util.concurrent.ConcurrentHashMap<>();

    @Async
    public void recordClick(String shortCode) {
        if ("local".equalsIgnoreCase(counterMode)) {
            localTotal.computeIfAbsent(shortCode, k -> new java.util.concurrent.atomic.AtomicLong()).incrementAndGet();
            localDay.computeIfAbsent(shortCode, k -> new java.util.concurrent.atomic.AtomicLong()).incrementAndGet();
            return;
        }
        String totalKey = "tf:clicks:total";
        String dayKey = "tf:clicks:day:" + LocalDate.now();
        try {
            var script = new org.springframework.data.redis.core.script.DefaultRedisScript<Long>();
            script.setScriptText("redis.call('HINCRBY', KEYS[1], ARGV[1], 1); redis.call('HINCRBY', KEYS[2], ARGV[1], 1); return 1;");
            script.setResultType(Long.class);
            java.util.List<String> keys = java.util.Arrays.asList(dayKey, totalKey);
            redisTemplate.execute(script, keys, shortCode);
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
        try { redisTemplate.rename(totalKey, tmpTotal); } catch (Exception ignored) {}
        try { redisTemplate.rename(dayKey, tmpDay); } catch (Exception ignored) {}
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
        try { redisTemplate.delete(tmpTotal); } catch (Exception ignored) {}
        try { redisTemplate.delete(tmpDay); } catch (Exception ignored) {}
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        try { return Long.parseLong(String.valueOf(value)); } catch (Exception ex) { return 0L; }
    }
}
