package com.layor.tinyflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短链接消息实体
 * 用于 RabbitMQ 消息传递，异步持久化短链接
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 短链接码
     */
    private String shortCode;
    
    /**
     * 长链接
     */
    private String longUrl;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 重试次数
     */
    @Builder.Default
    private Integer retryCount = 0;
}

