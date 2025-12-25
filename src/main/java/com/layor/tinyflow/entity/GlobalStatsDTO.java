package com.layor.tinyflow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 全局统计概览DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalStatsDTO {
    // 基础汇总
    private long totalUrls;       // 总短链数
    private long totalClicks;     // 总点击数(PV)
    private long totalUniqueIps;  // 总独立访客(UV)
    private long todayClicks;     // 今日点击
    private long activeUrls;      // 活跃短链数(今日有点击)
    
    // 趋势数据
    private List<KeyCountDTO> dailyTrend;    // 日趋势
    
    // 分布数据
    private List<KeyCountDTO> deviceDistribution;  // 设备分布
    private List<KeyCountDTO> cityTop10;           // 城市TOP10
    private List<KeyCountDTO> sourceTop10;         // 来源TOP10
    
    // 排行榜
    private List<UrlRankDTO> topUrls;  // 热门短链TOP10
}
