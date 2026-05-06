package com.layor.tinyflow.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 性能监控切面：记录慢请求
 */
@Slf4j
@Aspect
@Component
public class PerformanceMonitorAspect {

    private static final org.slf4j.Logger PERF_LOG = LoggerFactory.getLogger("PERFORMANCE");
    private static final long SLOW_THRESHOLD_MS = 100; // 慢请求阈值：100ms

    /**
     * 监控 Controller 层所有方法
     */
    @Around("execution(* com.layor.tinyflow.Controller..*(..))")
    public Object monitorController(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitor(joinPoint, "Controller");
    }

    /**
     * 监控 Service 层所有方法
     */
    @Around("execution(* com.layor.tinyflow.service..*(..))")
    public Object monitorService(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitor(joinPoint, "Service");
    }

    /**
     * 通用监控逻辑
     */
    private Object monitor(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录慢请求
            if (duration > SLOW_THRESHOLD_MS) {
                PERF_LOG.warn("[{}] SLOW - {} took {}ms", layer, methodName, duration);
            } else {
                log.debug("[{}] {} took {}ms", layer, methodName, duration);
            }
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            PERF_LOG.error("[{}] ERROR - {} failed after {}ms: {}", 
                    layer, methodName, duration, e.getMessage());
            throw e;
        }
    }
}
