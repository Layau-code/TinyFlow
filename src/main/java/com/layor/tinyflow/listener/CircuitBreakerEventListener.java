package com.layor.tinyflow.listener;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 熔断器事件监听器：记录熔断状态变化
 */
@Slf4j
@Configuration
public class CircuitBreakerEventListener {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        
        // 注册所有熔断器的事件监听
        registry.getAllCircuitBreakers().forEach(circuitBreaker -> {
            circuitBreaker.getEventPublisher()
                .onStateTransition(this::onStateTransition)
                .onError(event -> log.error("CircuitBreaker [{}] recorded error: {}", 
                    event.getCircuitBreakerName(), event.getThrowable().getMessage()))
                .onSuccess(event -> log.debug("CircuitBreaker [{}] recorded success", 
                    event.getCircuitBreakerName()));
        });
        
        return registry;
    }

    /**
     * 熔断器状态转换事件处理
     */
    private void onStateTransition(CircuitBreakerOnStateTransitionEvent event) {
        CircuitBreaker.State fromState = event.getStateTransition().getFromState();
        CircuitBreaker.State toState = event.getStateTransition().getToState();
        
        log.warn("⚡ CircuitBreaker [{}] state changed: {} → {}", 
            event.getCircuitBreakerName(), fromState, toState);
        
        // 熔断打开时发送告警（可接入钉钉、企业微信等）
        if (toState == CircuitBreaker.State.OPEN) {
            log.error("🔴 ALERT: CircuitBreaker [{}] is now OPEN! System degraded.", 
                event.getCircuitBreakerName());
            // TODO: 发送告警到监控平台
        }
        
        // 熔断恢复时记录日志
        if (toState == CircuitBreaker.State.CLOSED) {
            log.info("🟢 CircuitBreaker [{}] is now CLOSED. System recovered.", 
                event.getCircuitBreakerName());
        }
    }
}
