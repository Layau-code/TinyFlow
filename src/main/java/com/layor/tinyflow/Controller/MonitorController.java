package com.layor.tinyflow.Controller;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统监控端点
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    @Autowired(required = false)
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired(required = false)
    private RateLimiterRegistry rateLimiterRegistry;

    @Autowired
    @Qualifier("localUrlCache")
    private Cache<String, String> localCache;

    /**
     * 获取系统健康状态
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        
        // 熔断器状态
        if (circuitBreakerRegistry != null) {
            Map<String, String> breakers = new HashMap<>();
            circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
                breakers.put(cb.getName(), cb.getState().name());
            });
            health.put("circuitBreakers", breakers);
        }
        
        // 限流器状态
        if (rateLimiterRegistry != null) {
            Map<String, Object> limiters = new HashMap<>();
            rateLimiterRegistry.getAllRateLimiters().forEach(rl -> {
                Map<String, Object> metrics = new HashMap<>();
                metrics.put("availablePermissions", rl.getMetrics().getAvailablePermissions());
                metrics.put("waitingThreads", rl.getMetrics().getNumberOfWaitingThreads());
                limiters.put(rl.getName(), metrics);
            });
            health.put("rateLimiters", limiters);
        }
        
        // 缓存统计
        com.github.benmanes.caffeine.cache.stats.CacheStats stats = localCache.stats();
        Map<String, Object> cacheMetrics = new HashMap<>();
        cacheMetrics.put("hitCount", stats.hitCount());
        cacheMetrics.put("missCount", stats.missCount());
        cacheMetrics.put("hitRate", String.format("%.2f%%", stats.hitRate() * 100));
        cacheMetrics.put("evictionCount", stats.evictionCount());
        cacheMetrics.put("size", localCache.estimatedSize());
        health.put("caffeineCache", cacheMetrics);
        
        return health;
    }

    /**
     * 获取缓存详细信息
     */
    @GetMapping("/cache/stats")
    public Map<String, Object> cacheStats() {
        com.github.benmanes.caffeine.cache.stats.CacheStats stats = localCache.stats();
        Map<String, Object> result = new HashMap<>();
        result.put("hitCount", stats.hitCount());
        result.put("missCount", stats.missCount());
        result.put("loadSuccessCount", stats.loadSuccessCount());
        result.put("loadFailureCount", stats.loadFailureCount());
        result.put("totalLoadTime", stats.totalLoadTime());
        result.put("evictionCount", stats.evictionCount());
        result.put("evictionWeight", stats.evictionWeight());
        result.put("hitRate", stats.hitRate());
        result.put("missRate", stats.missRate());
        result.put("averageLoadPenalty", stats.averageLoadPenalty());
        result.put("estimatedSize", localCache.estimatedSize());
        return result;
    }

    /**
     * 清空本地缓存
     */
    @GetMapping("/cache/clear")
    public Map<String, Object> clearCache() {
        long sizeBefore = localCache.estimatedSize();
        localCache.invalidateAll();
        log.info("Local cache cleared, {} entries removed", sizeBefore);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("removedEntries", sizeBefore);
        result.put("message", "Cache cleared successfully");
        return result;
    }
}
