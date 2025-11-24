package com.layor.tinyflow.dto;

import lombok.Data;

@Data
public class StatsQuery {
    private String code;
    private String start;
    private String end;
    private String source;
    private String device;
    private String city;
    private Integer page;
    private Integer size;
}
