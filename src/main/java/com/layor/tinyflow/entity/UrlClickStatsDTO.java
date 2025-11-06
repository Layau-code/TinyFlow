package com.layor.tinyflow.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlClickStatsDTO {
    private String shortCode;     // 短链标识
    private Integer totalVisits;     // 总点击次数
    private Integer todayVisits;     // 今日点击次数
}