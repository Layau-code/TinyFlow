package com.layor.tinyflow.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Value("${cache.caffeine.spec:maximumSize=10000,expireAfterWrite=10m,recordStats}")
    private String cacheSpec;

    @Bean("localUrlCache")
    public Cache<String, String> localUrlCache() {
        return Caffeine.from(cacheSpec).build();
    }
}
