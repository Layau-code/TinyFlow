package com.layor.tinyflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    /**
     * JWT Token
     */
    private String token;
    
    /**
     * Token 类型
     */
    @Builder.Default
    private String type = "Bearer";
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户角色
     */
    private String role;
    
    /**
     * Token 过期时间（秒）
     */
    private Long expiresIn;
}
