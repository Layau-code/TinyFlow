package com.layor.tinyflow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * URL排行榜DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UrlRankDTO {
    private String shortCode;     // 短码
    private String longUrl;       // 原始URL
    private long totalClicks;     // 总点击数
    private int todayClicks;      // 今日点击数
}
