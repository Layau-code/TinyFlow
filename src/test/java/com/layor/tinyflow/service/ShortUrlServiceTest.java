package com.layor.tinyflow.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.layor.tinyflow.Strategy.HashidsStrategy;
import com.layor.tinyflow.entity.ShortUrl;
import com.layor.tinyflow.entity.ShortUrlDTO;
import com.layor.tinyflow.repository.ClickEventRepository;
import com.layor.tinyflow.repository.DailyClickRepository;
import com.layor.tinyflow.repository.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ShortUrlService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("短链服务测试")
class ShortUrlServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private DailyClickRepository dailyClickRepo;

    @Mock
    private ClickEventRepository clickEventRepository;

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
    @DisplayName("创建短链 - 失败（未登录）")
    void testCreateShortUrl_Fail_NotLoggedIn() {
        // Given
        when(authService.getCurrentUserId()).thenReturn(null);

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            shortUrlService.createShortUrl(TEST_LONG_URL, null);
        });
        
        assertEquals("未登录，请先登录", exception.getMessage());
    }

    @Test
    @DisplayName("创建短链 - 失败（无效的URL）")
    void testCreateShortUrl_Fail_InvalidUrl() {
        // Given
        String invalidUrl = "not-a-valid-url";
        when(authService.getCurrentUserId()).thenReturn(TEST_USER_ID);

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            shortUrlService.createShortUrl(invalidUrl, null);
        });
        
        assertEquals("长链接格式不正确", exception.getMessage());
    }

    @Test
    @DisplayName("创建短链 - 长链已存在且属于当前用户，直接返回")
    void testCreateShortUrl_ExistingLongUrl_SameUser() throws Exception {
        // Given
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
}
