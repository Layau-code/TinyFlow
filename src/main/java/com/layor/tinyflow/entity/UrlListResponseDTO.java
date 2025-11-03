package com.layor.tinyflow.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UrlListResponseDTO {

    private String shortCode;     // 短链码
    private String longUrl;       // 原始长链接
    private Long totalVisits;     // 总访问次数
    private Long todayVisits;     // 今日访问次数
    private LocalDateTime createdAt; // 创建时间
}