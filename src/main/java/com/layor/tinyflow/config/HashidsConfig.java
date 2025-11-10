package com.layor.tinyflow.config;

import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HashidsConfig {

    @Value("${shortcode.salt:mySecretSalt2025}") // 建议配置到 application.yml
    private String salt;

    @Value("${shortcode.minLength:4}")
    private int minLength;

    @Bean
    public Hashids hashids() {
        return new Hashids(salt, minLength,
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
    }
}