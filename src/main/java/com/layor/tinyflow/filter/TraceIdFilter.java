package com.layor.tinyflow.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * 链路追踪过滤器：为每个请求生成唯一 TraceId
 */
@Slf4j
@Component
public class TraceIdFilter implements Filter {

    private static final String TRACE_ID = "traceId";
    private static final String X_TRACE_ID = "X-Trace-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // 1. 尝试从请求头获取 TraceId（支持分布式追踪）
        String traceId = httpRequest.getHeader(X_TRACE_ID);
        
        // 2. 如果没有则生成新的 TraceId
        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }
        
        // 3. 放入 MDC（日志上下文）
        MDC.put(TRACE_ID, traceId);
        
        try {
            // 4. 继续过滤器链
            chain.doFilter(request, response);
        } finally {
            // 5. 清理 MDC（防止内存泄漏）
            MDC.remove(TRACE_ID);
        }
    }

    /**
     * 生成 TraceId：UUID 去掉横线
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
