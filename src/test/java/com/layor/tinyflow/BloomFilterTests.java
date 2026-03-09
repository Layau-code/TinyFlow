package com.layor.tinyflow;

import com.layor.tinyflow.service.ShortUrlService;
import com.layor.tinyflow.repository.ShortUrlRepository;
import com.layor.tinyflow.repository.DailyClickRepository;
import com.layor.tinyflow.entity.ShortUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 布隆过滤器单元测试
 * 验证防缓存穿透能力
 */
@SpringBootTest
@TestPropertySource(properties = {
    "bloom.warmup.enabled=true",
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379"
})
class BloomFilterTests {

    @MockBean
    private ShortUrlRepository shortUrlRepository;

    @MockBean
    private DailyClickRepository dailyClickRepository;

    @Autowired(required = false)
    private RBloomFilter<String> shorturlBloomFilter;

    @Autowired
    private ShortUrlService shortUrlService;

    private static final String VALID_SHORT_CODE = "abc123";
    private static final String INVALID_SHORT_CODE = "xyz999";

    @BeforeEach
    void setUp() {
        // 重置mock
        reset(shortUrlRepository, dailyClickRepository);
    }

    /**
     * 测试1：有效短码在布隆过滤器中
     */
    @Test
    void testValidShortCodeInBloomFilter() {
        if (shorturlBloomFilter == null) {
            System.out.println("⚠ 跳过测试：Redis/Redisson 未连接");
            return;
        }

        // 清空布隆过滤器
        shorturlBloomFilter.delete();

        // 添加有效短码
        shorturlBloomFilter.add(VALID_SHORT_CODE);

        // 验证：短码存在
        assertTrue(shorturlBloomFilter.contains(VALID_SHORT_CODE),
                "有效短码应该在布隆过滤器中");

        System.out.println("✓ 测试1通过：有效短码成功添加到布隆过滤器");
    }

    /**
     * 测试2：无效短码不在布隆过滤器中
     */
    @Test
    void testInvalidShortCodeNotInBloomFilter() {
        if (shorturlBloomFilter == null) {
            System.out.println("⚠ 跳过测试：Redis/Redisson 未连接");
            return;
        }

        // 清空布隆过滤器
        shorturlBloomFilter.delete();

        // 只添加有效短码
        shorturlBloomFilter.add(VALID_SHORT_CODE);

        // 验证：无效短码不存在（100%准确，无误判）
        assertFalse(shorturlBloomFilter.contains(INVALID_SHORT_CODE),
                "无效短码不应该在布隆过滤器中");

        System.out.println("✓ 测试2通过：无效短码被成功拦截");
    }

    /**
     * 测试3：缓存穿透防护
     * 当请求无效短码时，布隆过滤器直接返回null，避免查询缓存和数据库
     */
    @Test
    void testCachePenetrationPrevention() {
        if (shorturlBloomFilter == null) {
            System.out.println("⚠ 跳过测试：Redis/Redisson 未连接");
            return;
        }

        // 清空布隆过滤器
        shorturlBloomFilter.delete();

        // 只添加有效短码
        shorturlBloomFilter.add(VALID_SHORT_CODE);

        // 配置mock：当查询数据库时抛出异常（不应被调用）
        when(shortUrlRepository.findByShortCode(INVALID_SHORT_CODE))
                .thenThrow(new RuntimeException("Database should not be queried for invalid code"));

        // 调用service的getLongUrlByShortCode
        String result = shortUrlService.getLongUrlByShortCode(INVALID_SHORT_CODE);

        // 验证结果
        assertNull(result, "无效短码应返回null");

        // 验证数据库未被查询（缓存穿透被阻止）
        verify(shortUrlRepository, never()).findByShortCode(INVALID_SHORT_CODE);

        System.out.println("✓ 测试3通过：缓存穿透被成功防护（数据库未被查询）");
    }

    /**
     * 测试4：批量添加短码性能测试
     */
    @Test
    void testBatchAddPerformance() {
        if (shorturlBloomFilter == null) {
            System.out.println("⚠ 跳过测试：Redis/Redisson 未连接");
            return;
        }

        // 清空布隆过滤器
        shorturlBloomFilter.delete();

        // 批量添加10000条短码
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            shorturlBloomFilter.add("code_" + i);
        }
        long addTime = System.currentTimeMillis() - startTime;

        // 批量查询10000条
        startTime = System.currentTimeMillis();
        int hitCount = 0;
        for (int i = 0; i < 10000; i++) {
            if (shorturlBloomFilter.contains("code_" + i)) {
                hitCount++;
            }
        }
        long queryTime = System.currentTimeMillis() - startTime;

        System.out.println(String.format(
                "✓ 测试4通过：批量添加10000条耗时%dms，查询10000条耗时%dms，命中率%d%%",
                addTime, queryTime, (hitCount * 100) / 10000));

        assertTrue(hitCount >= 9990, "命中率应该>99%");
    }
}
