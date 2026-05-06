package com.layor.tinyflow.Controller;

import com.layor.tinyflow.entity.AuthRequest;
import com.layor.tinyflow.entity.AuthResponse;
import com.layor.tinyflow.entity.Result;
import com.layor.tinyflow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * 认证控制器
 * 处理用户注册、登录请求
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Result<AuthResponse>> register(
            @Valid @RequestBody AuthRequest request,
            BindingResult bindingResult
    ) {
        // 参数校验
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            log.warn("注册参数校验失败: {}", errors);
            return ResponseEntity.badRequest()
                    .body(Result.<AuthResponse>builder()
                            .success(false)
                            .message(errors)
                            .build());
        }
        
        // 邮箱必填
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Result.<AuthResponse>builder()
                            .success(false)
                            .message("邮箱不能为空")
                            .build());
        }
        
        try {
            AuthResponse response = authService.register(request);
            log.info("用户注册成功: {}", request.getUsername());
            
            return ResponseEntity.ok(Result.<AuthResponse>builder()
                    .success(true)
                    .message("注册成功")
                    .data(response)
                    .build());
                    
        } catch (IllegalArgumentException e) {
            log.warn("注册失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Result.<AuthResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
                            
        } catch (Exception e) {
            log.error("注册异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.<AuthResponse>builder()
                            .success(false)
                            .message("注册失败，请稍后重试")
                            .build());
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Result<AuthResponse>> login(
            @Valid @RequestBody AuthRequest request,
            BindingResult bindingResult
    ) {
        // 参数校验
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            log.warn("登录参数校验失败: {}", errors);
            return ResponseEntity.badRequest()
                    .body(Result.<AuthResponse>builder()
                            .success(false)
                            .message(errors)
                            .build());
        }
        
        try {
            AuthResponse response = authService.login(request);
            log.info("用户登录成功: {}", request.getUsername());
            
            return ResponseEntity.ok(Result.<AuthResponse>builder()
                    .success(true)
                    .message("登录成功")
                    .data(response)
                    .build());
                    
        } catch (BadCredentialsException e) {
            log.warn("登录失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.<AuthResponse>builder()
                            .success(false)
                            .message("用户名或密码错误")
                            .build());
                            
        } catch (Exception e) {
            log.error("登录异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.<AuthResponse>builder()
                            .success(false)
                            .message("登录失败，请稍后重试")
                            .build());
        }
    }
    
    /**
     * 获取当前用户信息（需要认证）
     */
    @GetMapping("/me")
    public ResponseEntity<Result<Object>> getCurrentUser() {
        try {
            var user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.builder()
                                .success(false)
                                .message("未登录")
                                .build());
            }
            
            return ResponseEntity.ok(Result.builder()
                    .success(true)
                    .data(java.util.Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "email", user.getEmail(),
                            "role", user.getRole(),
                            "createdAt", user.getCreatedAt()
                    ))
                    .build());
                    
        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.builder()
                            .success(false)
                            .message("获取用户信息失败")
                            .build());
        }
    }
}
