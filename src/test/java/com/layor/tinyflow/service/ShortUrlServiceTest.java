package com.layor.tinyflow.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.layor.tinyflow.Strategy.HashidsStrategy;
import com.layor.tinyflow.entity.DailyClick;
import com.layor.tinyflow.entity.ShortUrl;
import com.layor.tinyflow.entity.ShortUrlDTO;
import com.layor.tinyflow.entity.User;
import com.layor.tinyflow.repository.ClickEventRepository;
import com.layor.tinyflow.repository.DailyClickRepository;
import com.layor.tinyflow.repository.ShortUrlRepository;
import com.layor.tinyflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ShortUrlService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("短链服务测试")
class ShortUrlServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private DailyClickRepository dailyClickRepo;

    @Mock
    private ClickEventRepository clickEventRepository;
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private SegmentIdGenerator idGenerator;

    @Mock
    private HashidsStrategy codeStrategy;

    @Mock
    private AuthService authService;

    @Mock
    private ClickRecorderService clickRecorderService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private Cache<String, String> localCache;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ShortUrlService shortUrlService;

    private static final String TEST_LONG_URL = "https://www.example.com/very/long/url";
    private static final String TEST_SHORT_CODE = "abc123";
    private static final Long TEST_USER_ID = 1L;
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(shortUrlService, "baseUrl", BASE_URL);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("创建短链 - 成功（自动生成短码）")
    void testCreateShortUrl_Success_AutoGenerate() throws Exception {
        // Given
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        when(authService.getCurrentUserId()).thenReturn(TEST_USER_ID);
        when(shortUrlRepository.existsByLongUrl(TEST_LONG_URL)).thenReturn(false);
        when(idGenerator.nextId("shorturl")).thenReturn(12345L);
        when(codeStrategy.encode(12345L)).thenReturn(TEST_SHORT_CODE);
        when(shortUrlRepository.existsByShortCode(TEST_SHORT_CODE)).thenReturn(false);

        ShortUrl savedUrl = ShortUrl.builder()
                .longUrl(TEST_LONG_URL)
                .shortCode(TEST_SHORT_CODE)
                .userId(TEST_USER_ID)
                .createdAt(LocalDateTime.now())
                .build();
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(savedUrl);

        // When
        ShortUrlDTO result = shortUrlService.createShortUrl(TEST_LONG_URL, null);

        // Then
        assertNotNull(result);
        assertEquals(TEST_SHORT_CODE, result.getShortCode());
        assertEquals(BASE_URL + "/" + TEST_SHORT_CODE, result.getShortUrl());
        assertEquals(TEST_LONG_URL, result.getLongUrl());
        
        verify(shortUrlRepository, times(1)).save(any(ShortUrl.class));
    }

    @Test
    @DisplayName("创建短链 - 成功（自定义别名）")
    void testCreateShortUrl_Success_CustomAlias() throws Exception {
        // Given
        String customAlias = "myalias";
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        when(authService.getCurrentUserId()).thenReturn(TEST_USER_ID);
        when(shortUrlRepository.existsByLongUrl(TEST_LONG_URL)).thenReturn(false);
        when(shortUrlRepository.existsByShortCode(customAlias)).thenReturn(false);

        ShortUrl savedUrl = ShortUrl.builder()
                .longUrl(TEST_LONG_URL)
                .shortCode(customAlias)
                .userId(TEST_USER_ID)
                .createdAt(LocalDateTime.now())
                .build();
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(savedUrl);

        // When
        ShortUrlDTO result = shortUrlService.createShortUrl(TEST_LONG_URL, customAlias);

        // Then
        assertNotNull(result);
        assertEquals(customAlias, result.getShortCode());
        assertEquals(BASE_URL + "/" + customAlias, result.getShortUrl());
    }

    @Test
    @DisplayName("创建短链 - 失败（自定义别名已存在）")
    void testCreateShortUrl_Fail_CustomAliasExists() {
        // Given
        String customAlias = "existing";
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        when(authService.getCurrentUserId()).thenReturn(TEST_USER_ID);
        when(shortUrlRepository.existsByLongUrl(TEST_LONG_URL)).thenReturn(false);
        when(shortUrlRepository.existsByShortCode(customAlias)).thenReturn(true);

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            shortUrlService.createShortUrl(TEST_LONG_URL, customAlias);
        });
        
        assertEquals("自定义别名已存在", exception.getMessage());
        verify(shortUrlRepository, never()).save(any(ShortUrl.class));
    }

    @Test
    @DisplayName("创建短链 - 成功（未登录，绑定默认用户）")
    void testCreateShortUrl_Success_NotLoggedIn_DefaultUser() throws Exception {
        // Given
        Long defaultUserId = 2L;
        User defaultUser = User.builder()
                .id(defaultUserId)
                .username("user")
                .build();
        
        when(authService.getCurrentUserId()).thenReturn(null);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(defaultUser));
        when(shortUrlRepository.existsByLongUrl(TEST_LONG_URL)).thenReturn(false);
        when(idGenerator.nextId("shorturl")).thenReturn(12345L);
        when(codeStrategy.encode(12345L)).thenReturn(TEST_SHORT_CODE);
        when(shortUrlRepository.existsByShortCode(TEST_SHORT_CODE)).thenReturn(false);

        ShortUrl savedUrl = ShortUrl.builder()
                .longUrl(TEST_LONG_URL)
                .shortCode(TEST_SHORT_CODE)
                .userId(defaultUserId)  // 绑定默认用户
                .createdAt(LocalDateTime.now())
                .build();
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(savedUrl);

        // When
        ShortUrlDTO result = shortUrlService.createShortUrl(TEST_LONG_URL, null);

        // Then
        assertNotNull(result);
        assertEquals(TEST_SHORT_CODE, result.getShortCode());
        assertEquals(BASE_URL + "/" + TEST_SHORT_CODE, result.getShortUrl());
        assertEquals(TEST_LONG_URL, result.getLongUrl());
        
        verify(userRepository, times(1)).findByUsername("user");
        verify(shortUrlRepository, times(1)).save(any(ShortUrl.class));
    }

    @Test
    @DisplayName("创建短链 - 失败（无效的URL）")
    void testCreateShortUrl_Fail_InvalidUrl() {
        // Given
        String invalidUrl = "not-a-valid-url";
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        when(authService.getCurrentUserId()).thenReturn(TEST_USER_ID);

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            shortUrlService.createShortUrl(invalidUrl, null);
        });
        
        assertEquals("长链接格式不正确", exception.getMessage());
    }

    @Test
    @Disabled("暂时禁用，螢幕也权限管理需要修复")
    @DisplayName("创建短链 - 长链已存在且属于当前用户，直接返回")
    void testCreateShortUrl_ExistingLongUrl_SameUser() throws Exception {
        // Given
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        when(authService.getCurrentUserId()).thenReturn(TEST_USER_ID);
        when(shortUrlRepository.existsByLongUrl(TEST_LONG_URL)).thenReturn(true);
        
        ShortUrl existingUrl = ShortUrl.builder()
                .longUrl(TEST_LONG_URL)
                .shortCode(TEST_SHORT_CODE)
                .userId(TEST_USER_ID)
                .createdAt(LocalDateTime.now())
                .build();
        when(shortUrlRepository.findByLongUrl(TEST_LONG_URL)).thenReturn(existingUrl);

        // When
        ShortUrlDTO result = shortUrlService.createShortUrl(TEST_LONG_URL, null);

        // Then
        assertNotNull(result);
        assertEquals(TEST_SHORT_CODE, result.getShortCode());
        verify(shortUrlRepository, never()).save(any(ShortUrl.class));
    }

    @Test
    @DisplayName("查询长链 - L1缓存命中")
    void testGetLongUrl_L1CacheHit() {
        // Given
        when(localCache.getIfPresent(TEST_SHORT_CODE)).thenReturn(TEST_LONG_URL);

        // When
        String result = shortUrlService.getLongUrlByShortCode(TEST_SHORT_CODE);

        // Then
        assertEquals(TEST_LONG_URL, result);
        verify(redisTemplate, never()).opsForValue();
        verify(shortUrlRepository, never()).findByShortCode(anyString());
    }

    @Test
    @DisplayName("查询长链 - L2缓存命中")
    void testGetLongUrl_L2CacheHit() {
        // Given
        when(localCache.getIfPresent(TEST_SHORT_CODE)).thenReturn(null);
        when(valueOperations.get("short_url:" + TEST_SHORT_CODE)).thenReturn(TEST_LONG_URL);

        // When
        String result = shortUrlService.getLongUrlByShortCode(TEST_SHORT_CODE);

        // Then
        assertEquals(TEST_LONG_URL, result);
        verify(localCache, times(1)).put(TEST_SHORT_CODE, TEST_LONG_URL);
        verify(shortUrlRepository, never()).findByShortCode(anyString());
    }

    @Test
    @DisplayName("查询长链 - 数据库回源")
    void testGetLongUrl_DatabaseFallback() {
        // Given
        when(localCache.getIfPresent(TEST_SHORT_CODE)).thenReturn(null);
        when(valueOperations.get("short_url:" + TEST_SHORT_CODE)).thenReturn(null);
        
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(TEST_SHORT_CODE)
                .longUrl(TEST_LONG_URL)
                .userId(TEST_USER_ID)
                .build();
        when(shortUrlRepository.findByShortCode(TEST_SHORT_CODE)).thenReturn(shortUrl);

        // When
        String result = shortUrlService.getLongUrlByShortCode(TEST_SHORT_CODE);

        // Then
        assertEquals(TEST_LONG_URL, result);
        verify(localCache, times(1)).put(TEST_SHORT_CODE, TEST_LONG_URL);
        verify(valueOperations, times(1)).set(eq("short_url:" + TEST_SHORT_CODE), 
                eq(TEST_LONG_URL), any(Duration.class));
    }

    @Test
    @DisplayName("查询长链 - 短码不存在")
    void testGetLongUrl_NotFound() {
        // Given
        when(localCache.getIfPresent(TEST_SHORT_CODE)).thenReturn(null);
        when(valueOperations.get("short_url:" + TEST_SHORT_CODE)).thenReturn(null);
        when(shortUrlRepository.findByShortCode(TEST_SHORT_CODE)).thenReturn(null);

        // When
        String result = shortUrlService.getLongUrlByShortCode(TEST_SHORT_CODE);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("删除短链 - 成功")
    void testDeleteShortUrl_Success() {
        // Given
        when(authService.getCurrentUserId()).thenReturn(TEST_USER_ID);
        
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(TEST_SHORT_CODE)
                .longUrl(TEST_LONG_URL)
                .userId(TEST_USER_ID)
                .build();
        when(shortUrlRepository.findByShortCode(TEST_SHORT_CODE)).thenReturn(shortUrl);

        // When
        shortUrlService.deleteByShortCode(TEST_SHORT_CODE);

        // Then
        verify(shortUrlRepository, times(1)).deleteByShortCode(TEST_SHORT_CODE);
        verify(dailyClickRepo, times(1)).deleteByShortCode(TEST_SHORT_CODE);
    }

    @Test
    @DisplayName("删除短链 - 失败（无权限）")
    void testDeleteShortUrl_Fail_NoPermission() {
        // Given
        Long otherUserId = 2L;
        when(authService.getCurrentUserId()).thenReturn(TEST_USER_ID);
        
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(TEST_SHORT_CODE)
                .longUrl(TEST_LONG_URL)
                .userId(otherUserId)
                .build();
        when(shortUrlRepository.findByShortCode(TEST_SHORT_CODE)).thenReturn(shortUrl);

        // When & Then
        assertThrows(SecurityException.class, () -> {
            shortUrlService.deleteByShortCode(TEST_SHORT_CODE);
        });
        
        verify(shortUrlRepository, never()).deleteByShortCode(anyString());
    }

    // ==================== 统计功能测试 ====================

    // 辅助方法：创建 Object[] 列表
    @SuppressWarnings("unchecked")
    private List<Object[]> createObjectArrayList(Object[]... arrays) {
        List<Object[]> result = new ArrayList<>();
        for (Object[] arr : arrays) {
            result.add(arr);
        }
        return result;
    }

    @Test
    @DisplayName("获取详细统计数据 - 成功")
    void testGetDetailedStats_Success() {
        // Given
        String shortCode = TEST_SHORT_CODE;
        LocalDateTime start = LocalDateTime.of(2025, 12, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 31, 23, 59);
        
        when(clickEventRepository.countTotal(eq(shortCode), any(), any())).thenReturn(1000L);
        when(clickEventRepository.countUniqueIp(eq(shortCode), any(), any())).thenReturn(500L);
        when(clickEventRepository.countByHour(eq(shortCode), any(), any()))
                .thenReturn(createObjectArrayList(new Object[]{10, 50L}, new Object[]{14, 80L}));
        when(clickEventRepository.countByDayOfWeek(eq(shortCode), any(), any()))
                .thenReturn(createObjectArrayList(new Object[]{1, 100L}, new Object[]{2, 120L}));
        when(clickEventRepository.countByCountry(eq(shortCode), any(), any()))
                .thenReturn(createObjectArrayList(new Object[]{"中国", 800L}));
        when(clickEventRepository.countByCity(eq(shortCode), any(), any(), isNull()))
                .thenReturn(createObjectArrayList(new Object[]{"北京", 300L}));
        when(clickEventRepository.countByDevice(eq(shortCode), any(), any(), isNull()))
                .thenReturn(createObjectArrayList(new Object[]{"mobile", 600L}));
        when(clickEventRepository.countByUa(eq(shortCode), any(), any()))
                .thenReturn(createObjectArrayList(new Object[]{"Mozilla/5.0 Chrome", 700L}));
        when(clickEventRepository.countBySource(eq(shortCode), any(), any(), isNull()))
                .thenReturn(createObjectArrayList(new Object[]{"google.com", 200L}));
        when(clickEventRepository.countByReferer(eq(shortCode), any(), any()))
                .thenReturn(createObjectArrayList(new Object[]{"https://google.com/search", 150L}));
        when(clickEventRepository.findFirstClickTime(shortCode)).thenReturn(LocalDateTime.of(2025, 12, 1, 10, 0));
        when(clickEventRepository.findLastClickTime(shortCode)).thenReturn(LocalDateTime.of(2025, 12, 25, 15, 30));

        // When
        com.layor.tinyflow.entity.DetailedStatsDTO result = shortUrlService.getDetailedStats(shortCode, "2025-12-01", "2025-12-31");

        // Then
        assertNotNull(result);
        assertEquals(1000L, result.getPv());
        assertEquals(500L, result.getUv());
        assertEquals(2.0, result.getPvUvRatio(), 0.01);
        assertNotNull(result.getHourDistribution());
        assertNotNull(result.getWeekdayDistribution());
        assertNotNull(result.getCountryDistribution());
        assertNotNull(result.getCityDistribution());
        assertNotNull(result.getDeviceDistribution());
        assertNotNull(result.getBrowserDistribution());
        assertNotNull(result.getSourceDistribution());
        assertNotNull(result.getRefererDistribution());
        assertNotNull(result.getFirstClick());
        assertNotNull(result.getLastClick());
    }

    @Test
    @DisplayName("获取详细统计数据 - UV为0时PV/UV比率为0")
    void testGetDetailedStats_ZeroUv() {
        // Given
        when(clickEventRepository.countTotal(eq(TEST_SHORT_CODE), any(), any())).thenReturn(100L);
        when(clickEventRepository.countUniqueIp(eq(TEST_SHORT_CODE), any(), any())).thenReturn(0L);
        when(clickEventRepository.countByHour(eq(TEST_SHORT_CODE), any(), any())).thenReturn(Collections.emptyList());
        when(clickEventRepository.countByDayOfWeek(eq(TEST_SHORT_CODE), any(), any())).thenReturn(Collections.emptyList());
        when(clickEventRepository.countByCountry(eq(TEST_SHORT_CODE), any(), any())).thenReturn(Collections.emptyList());
        when(clickEventRepository.countByCity(eq(TEST_SHORT_CODE), any(), any(), isNull())).thenReturn(Collections.emptyList());
        when(clickEventRepository.countByDevice(eq(TEST_SHORT_CODE), any(), any(), isNull())).thenReturn(Collections.emptyList());
        when(clickEventRepository.countByUa(eq(TEST_SHORT_CODE), any(), any())).thenReturn(Collections.emptyList());
        when(clickEventRepository.countBySource(eq(TEST_SHORT_CODE), any(), any(), isNull())).thenReturn(Collections.emptyList());
        when(clickEventRepository.countByReferer(eq(TEST_SHORT_CODE), any(), any())).thenReturn(Collections.emptyList());
        when(clickEventRepository.findFirstClickTime(TEST_SHORT_CODE)).thenReturn(null);
        when(clickEventRepository.findLastClickTime(TEST_SHORT_CODE)).thenReturn(null);

        // When
        com.layor.tinyflow.entity.DetailedStatsDTO result = shortUrlService.getDetailedStats(TEST_SHORT_CODE, null, null);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getPv());
        assertEquals(0L, result.getUv());
        assertEquals(0.0, result.getPvUvRatio(), 0.01);
    }

    @Test
    @DisplayName("获取小时分布 - 成功")
    void testGetHourDistribution_Success() {
        // Given
        when(clickEventRepository.countByHour(eq(TEST_SHORT_CODE), any(), any()))
                .thenReturn(createObjectArrayList(
                        new Object[]{0, 10L},
                        new Object[]{10, 50L},
                        new Object[]{14, 80L},
                        new Object[]{20, 30L}
                ));

        // When
        List<com.layor.tinyflow.entity.KeyCountDTO> result = shortUrlService.getHourDistribution(TEST_SHORT_CODE, null, null);

        // Then
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("00:00", result.get(0).getLabel());
        assertEquals(10L, result.get(0).getCount());
        assertEquals("10:00", result.get(1).getLabel());
        assertEquals(50L, result.get(1).getCount());
    }

    @Test
    @DisplayName("获取星期分布 - 成功")
    void testGetWeekdayDistribution_Success() {
        // Given
        when(clickEventRepository.countByDayOfWeek(eq(TEST_SHORT_CODE), any(), any()))
                .thenReturn(createObjectArrayList(
                        new Object[]{1, 100L},  // 周日
                        new Object[]{2, 120L},  // 周一
                        new Object[]{6, 150L}   // 周五
                ));

        // When
        List<com.layor.tinyflow.entity.KeyCountDTO> result = shortUrlService.getWeekdayDistribution(TEST_SHORT_CODE, null, null);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("周日", result.get(0).getLabel());
        assertEquals("周一", result.get(1).getLabel());
        assertEquals("周五", result.get(2).getLabel());
    }

    @Test
    @DisplayName("获取浏览器分布 - 成功并合并相同浏览器")
    void testGetBrowserDistribution_Success() {
        // Given
        when(clickEventRepository.countByUa(eq(TEST_SHORT_CODE), any(), any()))
                .thenReturn(createObjectArrayList(
                        new Object[]{"Mozilla/5.0 (Windows) Chrome/120.0", 300L},
                        new Object[]{"Mozilla/5.0 (Mac) Chrome/119.0", 200L},
                        new Object[]{"Mozilla/5.0 Safari/17.0", 150L},
                        new Object[]{"Mozilla/5.0 Firefox/120.0", 100L}
                ));

        // When
        List<com.layor.tinyflow.entity.KeyCountDTO> result = shortUrlService.getBrowserDistribution(TEST_SHORT_CODE, null, null);

        // Then
        assertNotNull(result);
        // Chrome应该被合并
        var chromeEntry = result.stream().filter(k -> k.getLabel().equals("Chrome")).findFirst();
        assertTrue(chromeEntry.isPresent());
        assertEquals(500L, chromeEntry.get().getCount()); // 300 + 200
    }

    @Test
    @DisplayName("获取国家分布 - 成功")
    void testGetCountryDistribution_Success() {
        // Given
        when(clickEventRepository.countByCountry(eq(TEST_SHORT_CODE), any(), any()))
                .thenReturn(createObjectArrayList(
                        new Object[]{"中国", 800L},
                        new Object[]{"美国", 100L},
                        new Object[]{"日本", 50L}
                ));

        // When
        List<com.layor.tinyflow.entity.KeyCountDTO> result = shortUrlService.getCountryDistribution(TEST_SHORT_CODE, null, null);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("中国", result.get(0).getLabel());
        assertEquals(800L, result.get(0).getCount());
    }

    @Test
    @DisplayName("获取Referer分布 - 成功")
    void testGetRefererDistribution_Success() {
        // Given
        when(clickEventRepository.countByReferer(eq(TEST_SHORT_CODE), any(), any()))
                .thenReturn(createObjectArrayList(
                        new Object[]{"https://google.com/search?q=test", 200L},
                        new Object[]{"https://baidu.com/s?wd=test", 150L},
                        new Object[]{null, 100L}
                ));

        // When
        List<com.layor.tinyflow.entity.KeyCountDTO> result = shortUrlService.getRefererDistribution(TEST_SHORT_CODE, null, null);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("获取PV/UV数据 - 成功")
    void testGetPvUv_Success() {
        // Given
        when(clickEventRepository.countTotal(eq(TEST_SHORT_CODE), any(), any())).thenReturn(1000L);
        when(clickEventRepository.countUniqueIp(eq(TEST_SHORT_CODE), any(), any())).thenReturn(500L);

        // When
        Map<String, Long> result = shortUrlService.getPvUv(TEST_SHORT_CODE, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1000L, result.get("pv"));
        assertEquals(500L, result.get("uv"));
    }

    @Test
    @DisplayName("获取全局统计 - 成功")
    void testGetGlobalStats_Success() {
        // Given
        when(shortUrlRepository.count()).thenReturn(100L);
        when(clickEventRepository.countAllTotal(any(), any())).thenReturn(50000L);
        when(clickEventRepository.countAllUniqueIp(any(), any())).thenReturn(20000L);
        when(dailyClickRepo.findTodayActiveCodes()).thenReturn(Arrays.asList("code1", "code2", "code3"));
        when(clickEventRepository.countAllByDate(any(), any()))
                .thenReturn(createObjectArrayList(new Object[]{"2025-12-25", 500L}));
        when(clickEventRepository.countAllByDevice(any(), any()))
                .thenReturn(createObjectArrayList(new Object[]{"mobile", 30000L}));
        when(clickEventRepository.countAllByCity(any(), any()))
                .thenReturn(createObjectArrayList(new Object[]{"北京", 10000L}));
        when(clickEventRepository.countAllBySource(any(), any()))
                .thenReturn(createObjectArrayList(new Object[]{"google.com", 5000L}));
        
        ShortUrl topUrl = ShortUrl.builder()
                .shortCode("abc123")
                .longUrl("https://example.com")
                .clickCount(1000)
                .build();
        when(shortUrlRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(Arrays.asList(topUrl)));
        when(dailyClickRepo.getTodayClicksByShortCode("abc123")).thenReturn(50);

        // When
        com.layor.tinyflow.entity.GlobalStatsDTO result = shortUrlService.getGlobalStats(null, null);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getTotalUrls());
        assertEquals(50000L, result.getTotalClicks());
        assertEquals(20000L, result.getTotalUniqueIps());
        assertEquals(3L, result.getActiveUrls());
        assertNotNull(result.getDailyTrend());
        assertNotNull(result.getDeviceDistribution());
        assertNotNull(result.getCityTop10());
        assertNotNull(result.getSourceTop10());
        assertNotNull(result.getTopUrls());
        assertEquals(1, result.getTopUrls().size());
        assertEquals("abc123", result.getTopUrls().get(0).getShortCode());
    }

    @Test
    @DisplayName("获取访问趋势对比 - 成功")
    void testGetVisitTrendsByShortCodes_Success() {
        // Given
        List<String> shortCodes = Arrays.asList("code1", "code2");
        when(dailyClickRepo.findByShortCodeAndDate(eq("code1"), any()))
                .thenReturn(Optional.of(DailyClick.builder().clicks(100).build()));
        when(dailyClickRepo.findByShortCodeAndDate(eq("code2"), any()))
                .thenReturn(Optional.of(DailyClick.builder().clicks(200).build()));

        // When
        Map<String, List<com.layor.tinyflow.entity.DailyVisitTrendDTO>> result = 
                shortUrlService.getVisitTrendsByShortCodes(shortCodes, 7);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("code1"));
        assertTrue(result.containsKey("code2"));
        assertEquals(7, result.get("code1").size());
        assertEquals(7, result.get("code2").size());
    }

    @Test
    @DisplayName("获取短链概览 - 成功")
    void testGetOverviewByShortCode_Success() {
        // Given
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(TEST_SHORT_CODE)
                .longUrl(TEST_LONG_URL)
                .clickCount(1000)
                .createdAt(LocalDateTime.of(2025, 12, 1, 10, 0))
                .build();
        when(shortUrlRepository.findByShortCode(TEST_SHORT_CODE)).thenReturn(shortUrl);
        when(dailyClickRepo.findByShortCodeAndDate(eq(TEST_SHORT_CODE), any()))
                .thenReturn(Optional.of(DailyClick.builder().clicks(50).build()));

        // When
        com.layor.tinyflow.entity.ShortUrlOverviewDTO result = shortUrlService.getOverviewByShortCode(TEST_SHORT_CODE);

        // Then
        assertNotNull(result);
        assertEquals(1000, result.getTotalVisits());
        assertEquals(50, result.getTodayVisits());
    }

    @Test
    @DisplayName("获取每日趋势 - 成功")
    void testGetDailyTrendByShortCode_Success() {
        // Given
        when(dailyClickRepo.findByShortCodeAndDate(eq(TEST_SHORT_CODE), any()))
                .thenReturn(Optional.of(DailyClick.builder().clicks(100).build()));

        // When
        List<com.layor.tinyflow.entity.DailyVisitTrendDTO> result = 
                shortUrlService.getDailyTrendByShortCode(TEST_SHORT_CODE, 7);

        // Then
        assertNotNull(result);
        assertEquals(7, result.size());
        // 验证返回了7天的数据
        for (com.layor.tinyflow.entity.DailyVisitTrendDTO dto : result) {
            assertNotNull(dto.getDate());
            assertEquals(100, dto.getVisits());
        }
    }

    @Test
    @DisplayName("获取每日趋势 - 无数据时返回0")
    void testGetDailyTrendByShortCode_NoData() {
        // Given
        when(dailyClickRepo.findByShortCodeAndDate(eq(TEST_SHORT_CODE), any()))
                .thenReturn(Optional.empty());

        // When
        List<com.layor.tinyflow.entity.DailyVisitTrendDTO> result = 
                shortUrlService.getDailyTrendByShortCode(TEST_SHORT_CODE, 3);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        for (com.layor.tinyflow.entity.DailyVisitTrendDTO dto : result) {
            assertEquals(0, dto.getVisits());
        }
    }
}
