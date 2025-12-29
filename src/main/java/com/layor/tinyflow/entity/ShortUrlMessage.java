package com.layor.tinyflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短链生成消息实体
 * 用于RabbitMQ异步持久化
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 短码
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
     * 重试次数（用于死信队列处理）
     */
    private Integer retryCount;
}
