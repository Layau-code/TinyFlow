package com.layor.tinyflow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DistributionDTO {
    private List<KeyCountDTO> referer;
    private List<KeyCountDTO> device;
    private List<KeyCountDTO> city;
}