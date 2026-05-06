package com.layor.tinyflow.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.layor.tinyflow.entity.ShortUrlDTO;
import com.layor.tinyflow.entity.ShortenRequest;
import com.layor.tinyflow.entity.UrlClickStatsDTO;
import com.layor.tinyflow.entity.UrlListResponseDTO;
import com.layor.tinyflow.service.ShortUrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ShortUrlController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("短链控制器测试")
class ShortUrlControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private ShortUrlService shortUrlService;

    @InjectMocks
    private ShortUrlController shortUrlController;

    private ShortenRequest shortenRequest;
    private ShortUrlDTO shortUrlDTO;
    private static final String TEST_LONG_URL = "https://www.example.com";
    private static final String TEST_SHORT_CODE = "abc123";
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // 支持 Java 8 日期类型
        
        mockMvc = MockMvcBuilders
                .standaloneSetup(shortUrlController)
                .build();
        
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
    @DisplayName("POST /api/shorten - 创建短链成功")
    void testShorten_Success() throws Exception {
        // Given
        when(shortUrlService.createShortUrl(TEST_LONG_URL, null))
                .thenReturn(shortUrlDTO);

        // When & Then
        mockMvc.perform(post("/api/shorten")
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shortCode").value(customAlias));

        verify(shortUrlService, times(1)).createShortUrl(TEST_LONG_URL, customAlias);
    }



    @Test
    @DisplayName("DELETE /api/{shortCode} - 删除短链成功")
    void testDeleteHistory_Success() throws Exception {
        // Given
        doNothing().when(shortUrlService).deleteByShortCode(TEST_SHORT_CODE);

        // When & Then
        mockMvc.perform(delete("/api/" + TEST_SHORT_CODE))
                .andExpect(status().isOk());

        verify(shortUrlService, times(1)).deleteByShortCode(TEST_SHORT_CODE);
    }

    @Test
    @DisplayName("PUT /api/{shortCode} - 更新短链别名")
    void testUpdateShortUrl_Success() throws Exception {
        // Given
        String newAlias = "newalias";
        shortenRequest.setShortCode(TEST_SHORT_CODE);
        shortenRequest.setCustomAlias(newAlias);
        
        doNothing().when(shortUrlService).updateShortUrl(TEST_SHORT_CODE, newAlias);

        // When & Then
        mockMvc.perform(put("/api/" + TEST_SHORT_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortenRequest)))
                .andExpect(status().isOk());

        verify(shortUrlService, times(1)).updateShortUrl(TEST_SHORT_CODE, newAlias);
    }

    @Test
    @DisplayName("GET /api/urls/click-stats - 获取点击统计")
    void testGetUrlClickStats_Success() throws Exception {
        // Given
        List<UrlClickStatsDTO> stats = Arrays.asList(
                new UrlClickStatsDTO("code1", 100, 10),
                new UrlClickStatsDTO("code2", 200, 20)
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