package com.layor.tinyflow.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

/**
 * 布隆过滤器配置（防止缓存穿透）
 */
@Configuration
@Slf4j
public class BloomFilterConfig {
    
    /**
     * 短链布隆过滤器
     * 预计存储：1000万条短链
     * 误判率：0.01%（万分之一）
     */
    @Bean
    public BloomFilter<String> shortCodeBloomFilter() {
        BloomFilter<String> bloomFilter = BloomFilter.create(
            Funnels.stringFunnel(StandardCharsets.UTF_8),
            10_000_000L,    // 预计1000万条
            0.0001          // 误判率0.01%
        );
        
        log.info("布隆过滤器初始化完成: expectedInsertions=10000000, fpp=0.0001");
        return bloomFilter;
    }
}
