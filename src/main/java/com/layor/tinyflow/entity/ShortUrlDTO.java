package com.layor.tinyflow.entity;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShortUrlDTO  {
    private String shortCode;
    private String shortUrl;
    private String longUrl;
    private LocalDateTime createdAt;
}