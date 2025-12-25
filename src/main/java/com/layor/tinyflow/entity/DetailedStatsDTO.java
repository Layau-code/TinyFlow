package com.layor.tinyflow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 详细统计数据DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailedStatsDTO {
    // 基础指标
    private long pv;           // 页面访问量
    private long uv;           // 独立访客数
    private double pvUvRatio;  // PV/UV比率
    
    // 时间分布
    private List<KeyCountDTO> hourDistribution;    // 24小时分布
    private List<KeyCountDTO> weekdayDistribution; // 星期分布
    
    // 地理分布
    private List<KeyCountDTO> countryDistribution; // 国家分布
    private List<KeyCountDTO> cityDistribution;    // 城市分布
    
    // 技术分布
    private List<KeyCountDTO> deviceDistribution;  // 设备分布
    private List<KeyCountDTO> browserDistribution; // 浏览器分布
    
    // 来源分布
    private List<KeyCountDTO> sourceDistribution;  // 来源域名分布
    private List<KeyCountDTO> refererDistribution; // Referer详细分布
    
    // 时间信息
    private LocalDateTime firstClick;  // 首次访问时间
    private LocalDateTime lastClick;   // 最后访问时间
}
