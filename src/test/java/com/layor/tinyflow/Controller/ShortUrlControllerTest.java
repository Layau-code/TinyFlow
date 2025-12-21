package com.layor.tinyflow.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.layor.tinyflow.entity.*;
import com.layor.tinyflow.service.ShortUrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ShortUrlController 集成测试
 */
@WebMvcTest(ShortUrlController.class)
@DisplayName("短链控制器测试")
class ShortUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShortUrlService shortUrlService;

    private ShortenRequest shortenRequest;
    private ShortUrlDTO shortUrlDTO;
    private static final String TEST_LONG_URL = "https://www.example.com";
    private static final String TEST_SHORT_CODE = "abc123";
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        shortenRequest = new ShortenRequest();
        shortenRequest.setLongUrl(TEST_LONG_URL);

        shortUrlDTO = ShortUrlDTO.builder()
                .shortCode(TEST_SHORT_CODE)
                .shortUrl(BASE_URL + "/" + TEST_SHORT_CODE)
                .longUrl(TEST_LONG_URL)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/shorten - 创建短链成功")
    void testShorten_Success() throws Exception {
        // Given
        when(shortUrlService.createShortUrl(TEST_LONG_URL, null))
                .thenReturn(shortUrlDTO);

        // When & Then
        mockMvc.perform(post("/api/shorten")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shortCode").value(TEST_SHORT_CODE))
                .andExpect(jsonPath("$.data.longUrl").value(TEST_LONG_URL))
                .andExpect(jsonPath("$.data.shortUrl").value(BASE_URL + "/" + TEST_SHORT_CODE));

        verify(shortUrlService, times(1)).createShortUrl(TEST_LONG_URL, null);
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/shorten - 使用自定义别名")
    void testShorten_WithCustomAlias() throws Exception {
        // Given
        String customAlias = "myalias";
        shortenRequest.setCustomAlias(customAlias);
        
        ShortUrlDTO customDto = ShortUrlDTO.builder()
                .shortCode(customAlias)
                .shortUrl(BASE_URL + "/" + customAlias)
                .longUrl(TEST_LONG_URL)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(shortUrlService.createShortUrl(TEST_LONG_URL, customAlias))
                .thenReturn(customDto);

        // When & Then
        mockMvc.perform(post("/api/shorten")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shortCode").value(customAlias));

        verify(shortUrlService, times(1)).createShortUrl(TEST_LONG_URL, customAlias);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/urls - 获取短链列表")
    void testGetUrls_Success() throws Exception {
        // Given
        List<UrlListResponseDTO> urlList = Arrays.asList(
                UrlListResponseDTO.builder()
                        .shortCode("code1")
                        .longUrl("https://example1.com")
                        .totalVisits(100L)
                        .todayVisits(10)
                        .createdAt(LocalDateTime.now())
                        .build(),
                UrlListResponseDTO.builder()
                        .shortCode("code2")
                        .longUrl("https://example2.com")
                        .totalVisits(200L)
                        .todayVisits(20)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<UrlListResponseDTO> page = new PageImpl<>(urlList, pageable, urlList.size());
        
        when(shortUrlService.getAllUrls(0, 10)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/urls")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));

        verify(shortUrlService, times(1)).getAllUrls(0, 10);
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/{shortCode} - 删除短链成功")
    void testDeleteHistory_Success() throws Exception {
        // Given
        doNothing().when(shortUrlService).deleteByShortCode(TEST_SHORT_CODE);

        // When & Then
        mockMvc.perform(delete("/api/" + TEST_SHORT_CODE)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(shortUrlService, times(1)).deleteByShortCode(TEST_SHORT_CODE);
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/{shortCode} - 更新短链别名")
    void testUpdateShortUrl_Success() throws Exception {
        // Given
        String newAlias = "newalias";
        shortenRequest.setShortCode(TEST_SHORT_CODE);
        shortenRequest.setCustomAlias(newAlias);
        
        doNothing().when(shortUrlService).updateShortUrl(TEST_SHORT_CODE, newAlias);

        // When & Then
        mockMvc.perform(put("/api/" + TEST_SHORT_CODE)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortenRequest)))
                .andExpect(status().isOk());

        verify(shortUrlService, times(1)).updateShortUrl(TEST_SHORT_CODE, newAlias);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/urls/click-stats - 获取点击统计")
    void testGetUrlClickStats_Success() throws Exception {
        // Given
        List<UrlClickStatsDTO> stats = Arrays.asList(
                new UrlClickStatsDTO("code1", 100L, 10),
                new UrlClickStatsDTO("code2", 200L, 20)
        );
        
        when(shortUrlService.getUrlClickStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/urls/click-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(shortUrlService, times(1)).getUrlClickStats();
    }
}
