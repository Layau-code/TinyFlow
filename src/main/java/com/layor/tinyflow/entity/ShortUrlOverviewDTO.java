package com.layor.tinyflow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShortUrlOverviewDTO {
    private long totalVisits;
    private int todayVisits;
    private Instant createdAt; // 或 LocalDateTime，根据你的数据库类型
}