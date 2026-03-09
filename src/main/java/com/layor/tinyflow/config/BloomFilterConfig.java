package com.layor.tinyflow.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 布隆过滤器配置
 * 用于防止缓存穿透：拦截无效短码请求，避免打穿数据库
 */
@Configuration
@Slf4j
public class BloomFilterConfig {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 创建短链接布隆过滤器Bean
     * 
     * 参数说明：
     * - expectedInsertions: 1000万 - 预期插入数据量
     * - falseProbability: 0.01 - 1%误判率
     */
    @Bean(name = "shorturlBloomFilter")
    public RBloomFilter<String> shorturlBloomFilter() {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("shorturl:bloom");
        
        // 只初始化一次，如果已存在则跳过
        if (!bloomFilter.isExists()) {
            bloomFilter.tryInit(10_000_000L, 0.01);
            log.info("✓ 短链接布隆过滤器初始化成功: expectedInsertions=10000000, falsePositiveRate=0.01");
        } else {
            log.info("✓ 短链接布隆过滤器已存在，跳过初始化");
        }
        
        return bloomFilter;
    }

    /**
     * 布隆过滤器初始化服务
     * 在应用启动时加载已有的短链到布隆过滤器中
     */
    @Component
    @Slf4j
    public static class BloomFilterInitializer {

        @Autowired
        private RBloomFilter<String> shorturlBloomFilter;

        @Autowired(required = false)
        private com.layor.tinyflow.repository.ShortUrlRepository shortUrlRepository;

        /**
         * 启动时预热布隆过滤器：加载所有已有的短码
         */
        @PostConstruct
        @ConditionalOnProperty(name = "bloom.warmup.enabled", havingValue = "true", matchIfMissing = true)
        public void initBloomFilterWithExistingData() {
            if (shortUrlRepository == null) {
                log.warn("⚠ ShortUrlRepository未注入，跳过布隆过滤器预热");
                return;
            }

            try {
                // 获取所有已有的短码
                var allShortUrls = shortUrlRepository.findAll();
                if (allShortUrls.isEmpty()) {
                    log.info("数据库中无短链数据，布隆过滤器预热完成（0条记录）");
                    return;
                }

                // 逐条加入布隆过滤器
                int count = 0;
                for (var shortUrl : allShortUrls) {
                    shorturlBloomFilter.add(shortUrl.getShortCode());
                    count++;
                }

                log.info("✓ 布隆过滤器预热完成: {} 条短码已加载", count);
            } catch (Exception e) {
                log.error("✗ 布隆过滤器预热失败: {}", e.getMessage(), e);
            }
        }
    }
}
