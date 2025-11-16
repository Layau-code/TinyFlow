package com.layor.tinyflow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeyCountDTO {
    private String label;
    private long count;
}