package com.layor.tinyflow.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class ShortenRequest
{
    @NotBlank(message = "长链接不能为空")
    private String longUrl;

    // 可选：自定义别名，支持中文
    private String customAlias;
}