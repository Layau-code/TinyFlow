package com.layor.tinyflow.entity;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShortUrlDTO  {
    private String shortCode;
    private String shortUrl;
    private String longUrl;
    private LocalDateTime createdAt;

    // 构造方法
    public ShortUrlDTO(String shortCode, String shortUrl, String longUrl) {
        this.shortCode = shortCode;
        this.shortUrl = shortUrl;
        this.longUrl = longUrl;
        this.createdAt = LocalDateTime.now();
    }
}