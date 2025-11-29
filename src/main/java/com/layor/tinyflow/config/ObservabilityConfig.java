package com.layor.tinyflow.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 可观测性配置：监控、指标、链路追踪
 */
@Slf4j
@Configuration
public class ObservabilityConfig {

    /**
     * 启用 @Timed 注解支持（用于方法级性能监控）
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * 注册线程池监控指标
     */
    @Bean
    public ExecutorServiceMetrics executorServiceMetrics(
            ThreadPoolTaskExecutor taskExecutor,
            MeterRegistry meterRegistry) {
        ExecutorServiceMetrics metrics = new ExecutorServiceMetrics(
                taskExecutor.getThreadPoolExecutor(),
                "async-executor",
                java.util.List.of(
                    io.micrometer.core.instrument.Tag.of("type", "async")
                )
        );
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    /**
     * 注册 Caffeine 缓存监控
     */
    @Bean
    public io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics caffeineCacheMetrics(
            com.github.benmanes.caffeine.cache.Cache<String, String> localUrlCache,
            MeterRegistry meterRegistry) {
        return new io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics(
                localUrlCache,
                "localUrlCache",
                java.util.Collections.singletonList(io.micrometer.core.instrument.Tag.of("cache", "l1"))
        );
    }
}
