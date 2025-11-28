package com.layor.tinyflow.service;

import com.layor.tinyflow.entity.AuthRequest;
import com.layor.tinyflow.entity.AuthResponse;
import com.layor.tinyflow.entity.User;
import com.layor.tinyflow.repository.UserRepository;
import com.layor.tinyflow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务
 * 处理用户注册、登录逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    /**
     * 用户注册
     */
    @Transactional
    public AuthResponse register(AuthRequest request) {
        log.info("用户注册: {}", request.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已被注册");
        }
        
        // 创建新用户
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role("USER") // 默认角色
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        
        userRepository.save(user);
        log.info("用户注册成功: {}", user.getUsername());
        
        // 生成 Token
        String token = jwtUtil.generateToken(user);
        
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .expiresIn(jwtUtil.getExpirationSeconds())
                .build();
    }
    
    /**
     * 用户登录
     */
    public AuthResponse login(AuthRequest request) {
        log.info("用户登录: {}", request.getUsername());
        
        try {
            // 使用 Spring Security 认证管理器进行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 获取认证后的用户信息
            User user = (User) authentication.getPrincipal();
            
            // 生成 Token
            String token = jwtUtil.generateToken(user);
            
            log.info("用户登录成功: {}, 角色: {}", user.getUsername(), user.getRole());
            
            return AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .role(user.getRole())
                    .expiresIn(jwtUtil.getExpirationSeconds())
                    .build();
                    
        } catch (BadCredentialsException e) {
            log.warn("登录失败: 用户名或密码错误 - {}", request.getUsername());
            throw new BadCredentialsException("用户名或密码错误");
        }
    }
    
    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * 获取当前登录用户ID
     */
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
    
    /**
     * 获取当前登录用户名
     */
    public String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }
}
