package com.layor.tinyflow.config;

import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HashidsConfig {

    @Bean
    public Hashids hashids(@Value("${shortcode.minLen:4}") int minLen) {
        return new Hashids("tinyflow", minLen);
    }
}