package com.layor.tinyflow.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class ShortenRequest
{
    //短链
    private String shortCode;

    private String longUrl;

    // 可选：自定义别名，支持中文
    private String customAlias;
}