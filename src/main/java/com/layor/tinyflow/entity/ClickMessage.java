package com.layor.tinyflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 点击事件消息实体
 * 用于 RabbitMQ 消息传递
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 短链接码
     */
    private String shortCode;
    
    /**
     * 点击时间戳
     */
    private Long timestamp;
    
    /**
     * 日期（用于每日统计）
     */
    private String date;
}
