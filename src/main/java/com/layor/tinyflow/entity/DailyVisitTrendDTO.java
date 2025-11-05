package com.layor.tinyflow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class DailyVisitTrendDTO {
    private String date;
    private int visits;
}