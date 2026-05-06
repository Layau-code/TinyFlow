package com.layor.tinyflow.security;

import com.layor.tinyflow.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 拦截每个请求，验证 JWT Token 并设置 Spring Security 上下文
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 从请求头中提取 Token
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        
        // 如果没有 Token 或格式不正确，直接放行（由 Security 配置决定是否需要认证）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // 提取 Token（去掉 "Bearer " 前缀）
            jwt = authHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
            
            // 如果用户名存在且当前上下文中没有认证信息
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // 加载用户详情
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 验证 Token
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    // 设置详情
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置到 Spring Security 上下文
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("JWT 认证成功: {}, URI: {}", username, request.getRequestURI());
                } else {
                    log.warn("JWT 验证失败: {}", username);
                }
            }
            
        } catch (Exception e) {
            log.error("JWT 过滤器异常: {}", e.getMessage());
        }
        
        // 继续过滤链
        filterChain.doFilter(request, response);
    }
}
