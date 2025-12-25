package com.layor.tinyflow.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.layor.tinyflow.dto.StatsQuery;
import com.layor.tinyflow.entity.*;
import com.layor.tinyflow.service.ShortUrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StatsController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("统计控制器测试")
class StatsControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ShortUrlService shortUrlService;

    @InjectMocks
    private StatsController statsController;

    private static final String TEST_SHORT_CODE = "abc123";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders
                .standaloneSetup(statsController)
                .build();
    }

    @Test
    @DisplayName("GET /api/stats/overview/{shortCode} - 获取短链概览成功")
    void testGetShortUrlOverview_Success() throws Exception {
        // Given
        ShortUrlOverviewDTO overview = new ShortUrlOverviewDTO(
                1000,
                50,
                java.time.Instant.now()
        );
        when(shortUrlService.getOverviewByShortCode(TEST_SHORT_CODE)).thenReturn(overview);

        // When & Then
        mockMvc.perform(get("/api/stats/overview/{shortCode}", TEST_SHORT_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVisits").value(1000))
                .andExpect(jsonPath("$.todayVisits").value(50));

        verify(shortUrlService, times(1)).getOverviewByShortCode(TEST_SHORT_CODE);
    }

    @Test
    @DisplayName("GET /api/stats/trend/{shortCode} - 获取访问趋势成功")
    void testGetTrend_Success() throws Exception {
        // Given
        List<DailyVisitTrendDTO> trend = Arrays.asList(
                new DailyVisitTrendDTO("2025-12-25", 100),
                new DailyVisitTrendDTO("2025-12-24", 80),
                new DailyVisitTrendDTO("2025-12-23", 120)
        );
        when(shortUrlService.getDailyTrendByShortCode(TEST_SHORT_CODE, 7)).thenReturn(trend);

        // When & Then
        mockMvc.perform(get("/api/stats/trend/{shortCode}", TEST_SHORT_CODE)
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].date").value("2025-12-25"))
                .andExpect(jsonPath("$[0].visits").value(100));

        verify(shortUrlService, times(1)).getDailyTrendByShortCode(TEST_SHORT_CODE, 7);
    }

    @Test
    @DisplayName("GET /api/stats/trend/{shortCode} - 使用默认天数")
    void testGetTrend_DefaultDays() throws Exception {
        // Given
        List<DailyVisitTrendDTO> trend = new ArrayList<>();
        when(shortUrlService.getDailyTrendByShortCode(TEST_SHORT_CODE, 7)).thenReturn(trend);

        // When & Then
        mockMvc.perform(get("/api/stats/trend/{shortCode}", TEST_SHORT_CODE))
                .andExpect(status().isOk());

        verify(shortUrlService, times(1)).getDailyTrendByShortCode(TEST_SHORT_CODE, 7);
    }

    @Test
    @DisplayName("GET /api/stats/compare - 对比多个短链趋势")
    void testCompareTrends_Success() throws Exception {
        // Given
        Map<String, List<DailyVisitTrendDTO>> result = new HashMap<>();
        result.put("code1", Arrays.asList(new DailyVisitTrendDTO("2025-12-25", 100)));
        result.put("code2", Arrays.asList(new DailyVisitTrendDTO("2025-12-25", 200)));
        
        when(shortUrlService.getVisitTrendsByShortCodes(any(), eq(7))).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/stats/compare")
                        .param("trends", "code1,code2")
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code1").isArray())
                .andExpect(jsonPath("$.code2").isArray());

        verify(shortUrlService, times(1)).getVisitTrendsByShortCodes(any(), eq(7));
    }

    @Test
    @DisplayName("POST /api/stats/distribution - 获取分布数据")
    void testGetDistribution_Success() throws Exception {
        // Given
        List<KeyCountDTO> referer = Arrays.asList(new KeyCountDTO("google.com", 50L));
        List<KeyCountDTO> device = Arrays.asList(new KeyCountDTO("mobile", 30L), new KeyCountDTO("desktop", 70L));
        List<KeyCountDTO> city = Arrays.asList(new KeyCountDTO("北京", 40L));
        DistributionDTO distribution = new DistributionDTO(referer, device, city);
        
        when(shortUrlService.getDistribution(anyString(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(distribution);

        StatsQuery query = new StatsQuery();
        query.setCode(TEST_SHORT_CODE);
        query.setStart("2025-12-01");
        query.setEnd("2025-12-25");

        // When & Then
        mockMvc.perform(post("/api/stats/distribution")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referer").isArray())
                .andExpect(jsonPath("$.device").isArray())
                .andExpect(jsonPath("$.city").isArray());

        verify(shortUrlService, times(1)).getDistribution(anyString(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/stats/events - 获取点击事件列表")
    void testGetEvents_Success() throws Exception {
        // Given
        List<ClickEventDTO> events = Arrays.asList(
                new ClickEventDTO(LocalDateTime.now(), "192.168.1.1", "google.com", "mobile", 
                        "北京", "中国", "Mozilla/5.0", "https://google.com"),
                new ClickEventDTO(LocalDateTime.now(), "192.168.1.2", "baidu.com", "desktop", 
                        "上海", "中国", "Chrome", "https://baidu.com")
        );
        
        when(shortUrlService.getEvents(anyString(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(events);

        StatsQuery query = new StatsQuery();
        query.setCode(TEST_SHORT_CODE);
        query.setPage(0);
        query.setSize(20);

        // When & Then
        mockMvc.perform(post("/api/stats/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(shortUrlService, times(1)).getEvents(anyString(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("GET /api/stats/detailed/{shortCode} - 获取详细统计")
    void testGetDetailedStats_Success() throws Exception {
        // Given
        DetailedStatsDTO detailed = new DetailedStatsDTO();
        detailed.setPv(1000L);
        detailed.setUv(500L);
        detailed.setPvUvRatio(2.0);
        detailed.setHourDistribution(Arrays.asList(new KeyCountDTO("10:00", 50L)));
        detailed.setWeekdayDistribution(Arrays.asList(new KeyCountDTO("周一", 100L)));
        detailed.setCountryDistribution(Arrays.asList(new KeyCountDTO("中国", 800L)));
        detailed.setCityDistribution(Arrays.asList(new KeyCountDTO("北京", 300L)));
        detailed.setDeviceDistribution(Arrays.asList(new KeyCountDTO("mobile", 600L)));
        detailed.setBrowserDistribution(Arrays.asList(new KeyCountDTO("Chrome", 700L)));
        detailed.setSourceDistribution(Arrays.asList(new KeyCountDTO("google.com", 200L)));
        detailed.setRefererDistribution(Arrays.asList(new KeyCountDTO("https://google.com/search", 150L)));
        detailed.setFirstClick(LocalDateTime.of(2025, 12, 1, 10, 0));
        detailed.setLastClick(LocalDateTime.of(2025, 12, 25, 15, 30));
        
        when(shortUrlService.getDetailedStats(TEST_SHORT_CODE, null, null)).thenReturn(detailed);

        // When & Then
        mockMvc.perform(get("/api/stats/detailed/{shortCode}", TEST_SHORT_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pv").value(1000))
                .andExpect(jsonPath("$.uv").value(500))
                .andExpect(jsonPath("$.pvUvRatio").value(2.0))
                .andExpect(jsonPath("$.hourDistribution").isArray())
                .andExpect(jsonPath("$.weekdayDistribution").isArray())
                .andExpect(jsonPath("$.countryDistribution").isArray())
                .andExpect(jsonPath("$.cityDistribution").isArray())
                .andExpect(jsonPath("$.deviceDistribution").isArray())
                .andExpect(jsonPath("$.browserDistribution").isArray());

        verify(shortUrlService, times(1)).getDetailedStats(TEST_SHORT_CODE, null, null);
    }

    @Test
    @DisplayName("GET /api/stats/detailed/{shortCode} - 带时间范围")
    void testGetDetailedStats_WithTimeRange() throws Exception {
        // Given
        DetailedStatsDTO detailed = new DetailedStatsDTO();
        detailed.setPv(500L);
        detailed.setUv(200L);
        detailed.setPvUvRatio(2.5);
        
        when(shortUrlService.getDetailedStats(TEST_SHORT_CODE, "2025-12-01", "2025-12-25"))
                .thenReturn(detailed);

        // When & Then
        mockMvc.perform(get("/api/stats/detailed/{shortCode}", TEST_SHORT_CODE)
                        .param("start", "2025-12-01")
                        .param("end", "2025-12-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pv").value(500))
                .andExpect(jsonPath("$.uv").value(200));

        verify(shortUrlService, times(1)).getDetailedStats(TEST_SHORT_CODE, "2025-12-01", "2025-12-25");
    }

    @Test
    @DisplayName("GET /api/stats/global - 获取全局统计")
    void testGetGlobalStats_Success() throws Exception {
        // Given
        GlobalStatsDTO global = new GlobalStatsDTO();
        global.setTotalUrls(100L);
        global.setTotalClicks(50000L);
        global.setTotalUniqueIps(20000L);
        global.setTodayClicks(500L);
        global.setActiveUrls(30L);
        global.setDailyTrend(Arrays.asList(new KeyCountDTO("2025-12-25", 500L)));
        global.setDeviceDistribution(Arrays.asList(new KeyCountDTO("mobile", 30000L)));
        global.setCityTop10(Arrays.asList(new KeyCountDTO("北京", 10000L)));
        global.setSourceTop10(Arrays.asList(new KeyCountDTO("google.com", 5000L)));
        global.setTopUrls(Arrays.asList(new UrlRankDTO("abc123", "https://example.com", 1000L, 50)));
        
        when(shortUrlService.getGlobalStats(null, null)).thenReturn(global);

        // When & Then
        mockMvc.perform(get("/api/stats/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUrls").value(100))
                .andExpect(jsonPath("$.totalClicks").value(50000))
                .andExpect(jsonPath("$.totalUniqueIps").value(20000))
                .andExpect(jsonPath("$.todayClicks").value(500))
                .andExpect(jsonPath("$.activeUrls").value(30))
                .andExpect(jsonPath("$.dailyTrend").isArray())
                .andExpect(jsonPath("$.deviceDistribution").isArray())
                .andExpect(jsonPath("$.cityTop10").isArray())
                .andExpect(jsonPath("$.sourceTop10").isArray())
                .andExpect(jsonPath("$.topUrls").isArray());

        verify(shortUrlService, times(1)).getGlobalStats(null, null);
    }

    @Test
    @DisplayName("GET /api/stats/hour/{shortCode} - 获取小时分布")
    void testGetHourDistribution_Success() throws Exception {
        // Given
        List<KeyCountDTO> hourData = Arrays.asList(
                new KeyCountDTO("00:00", 10L),
                new KeyCountDTO("10:00", 50L),
                new KeyCountDTO("14:00", 80L),
                new KeyCountDTO("20:00", 30L)
        );
        when(shortUrlService.getHourDistribution(TEST_SHORT_CODE, null, null)).thenReturn(hourData);

        // When & Then
        mockMvc.perform(get("/api/stats/hour/{shortCode}", TEST_SHORT_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[1].label").value("10:00"))
                .andExpect(jsonPath("$[1].count").value(50));

        verify(shortUrlService, times(1)).getHourDistribution(TEST_SHORT_CODE, null, null);
    }

    @Test
    @DisplayName("GET /api/stats/weekday/{shortCode} - 获取星期分布")
    void testGetWeekdayDistribution_Success() throws Exception {
        // Given
        List<KeyCountDTO> weekdayData = Arrays.asList(
                new KeyCountDTO("周一", 100L),
                new KeyCountDTO("周二", 120L),
                new KeyCountDTO("周三", 80L),
                new KeyCountDTO("周四", 90L),
                new KeyCountDTO("周五", 150L),
                new KeyCountDTO("周六", 200L),
                new KeyCountDTO("周日", 180L)
        );
        when(shortUrlService.getWeekdayDistribution(TEST_SHORT_CODE, null, null)).thenReturn(weekdayData);

        // When & Then
        mockMvc.perform(get("/api/stats/weekday/{shortCode}", TEST_SHORT_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[0].label").value("周一"));

        verify(shortUrlService, times(1)).getWeekdayDistribution(TEST_SHORT_CODE, null, null);
    }

    @Test
    @DisplayName("GET /api/stats/browser/{shortCode} - 获取浏览器分布")
    void testGetBrowserDistribution_Success() throws Exception {
        // Given
        List<KeyCountDTO> browserData = Arrays.asList(
                new KeyCountDTO("Chrome", 500L),
                new KeyCountDTO("Safari", 200L),
                new KeyCountDTO("Firefox", 100L),
                new KeyCountDTO("Edge", 80L)
        );
        when(shortUrlService.getBrowserDistribution(TEST_SHORT_CODE, null, null)).thenReturn(browserData);

        // When & Then
        mockMvc.perform(get("/api/stats/browser/{shortCode}", TEST_SHORT_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].label").value("Chrome"))
                .andExpect(jsonPath("$[0].count").value(500));

        verify(shortUrlService, times(1)).getBrowserDistribution(TEST_SHORT_CODE, null, null);
    }

    @Test
    @DisplayName("GET /api/stats/country/{shortCode} - 获取国家分布")
    void testGetCountryDistribution_Success() throws Exception {
        // Given
        List<KeyCountDTO> countryData = Arrays.asList(
                new KeyCountDTO("中国", 800L),
                new KeyCountDTO("美国", 100L),
                new KeyCountDTO("日本", 50L)
        );
        when(shortUrlService.getCountryDistribution(TEST_SHORT_CODE, null, null)).thenReturn(countryData);

        // When & Then
        mockMvc.perform(get("/api/stats/country/{shortCode}", TEST_SHORT_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].label").value("中国"))
                .andExpect(jsonPath("$[0].count").value(800));

        verify(shortUrlService, times(1)).getCountryDistribution(TEST_SHORT_CODE, null, null);
    }

    @Test
    @DisplayName("GET /api/stats/referer/{shortCode} - 获取Referer分布")
    void testGetRefererDistribution_Success() throws Exception {
        // Given
        List<KeyCountDTO> refererData = Arrays.asList(
                new KeyCountDTO("https://google.com/search?q=test", 200L),
                new KeyCountDTO("https://baidu.com/s?wd=test", 150L),
                new KeyCountDTO("(直接访问)", 100L)
        );
        when(shortUrlService.getRefererDistribution(TEST_SHORT_CODE, null, null)).thenReturn(refererData);

        // When & Then
        mockMvc.perform(get("/api/stats/referer/{shortCode}", TEST_SHORT_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));

        verify(shortUrlService, times(1)).getRefererDistribution(TEST_SHORT_CODE, null, null);
    }

    @Test
    @DisplayName("GET /api/stats/pvuv/{shortCode} - 获取PV/UV数据")
    void testGetPvUv_Success() throws Exception {
        // Given
        Map<String, Long> pvuvData = new HashMap<>();
        pvuvData.put("pv", 1000L);
        pvuvData.put("uv", 500L);
        when(shortUrlService.getPvUv(TEST_SHORT_CODE, null, null)).thenReturn(pvuvData);

        // When & Then
        mockMvc.perform(get("/api/stats/pvuv/{shortCode}", TEST_SHORT_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pv").value(1000))
                .andExpect(jsonPath("$.uv").value(500));

        verify(shortUrlService, times(1)).getPvUv(TEST_SHORT_CODE, null, null);
    }

    @Test
    @DisplayName("GET /api/stats/pvuv/{shortCode} - 带时间范围")
    void testGetPvUv_WithTimeRange() throws Exception {
        // Given
        Map<String, Long> pvuvData = new HashMap<>();
        pvuvData.put("pv", 500L);
        pvuvData.put("uv", 200L);
        when(shortUrlService.getPvUv(TEST_SHORT_CODE, "2025-12-01", "2025-12-15")).thenReturn(pvuvData);

        // When & Then
        mockMvc.perform(get("/api/stats/pvuv/{shortCode}", TEST_SHORT_CODE)
                        .param("start", "2025-12-01")
                        .param("end", "2025-12-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pv").value(500))
                .andExpect(jsonPath("$.uv").value(200));

        verify(shortUrlService, times(1)).getPvUv(TEST_SHORT_CODE, "2025-12-01", "2025-12-15");
    }

    @Test
    @DisplayName("POST /api/stats/export - CSV导出")
    void testExport_CSV() throws Exception {
        // Given
        String csvContent = "ts,ip,source_host,device_type,city,country,ua,referer\n";
        when(shortUrlService.exportStats(anyString(), any(), any(), any(), any(), any(), eq("csv")))
                .thenReturn(csvContent.getBytes());

        StatsQuery query = new StatsQuery();
        query.setCode(TEST_SHORT_CODE);

        // When & Then
        mockMvc.perform(post("/api/stats/export")
                        .param("format", "csv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", 
                        "attachment; filename=stats-" + TEST_SHORT_CODE + ".csv"));

        verify(shortUrlService, times(1)).exportStats(anyString(), any(), any(), any(), any(), any(), eq("csv"));
    }

    @Test
    @DisplayName("POST /api/stats/export - JSON导出")
    void testExport_JSON() throws Exception {
        // Given
        String jsonContent = "[]";
        when(shortUrlService.exportStats(anyString(), any(), any(), any(), any(), any(), eq("json")))
                .thenReturn(jsonContent.getBytes());

        StatsQuery query = new StatsQuery();
        query.setCode(TEST_SHORT_CODE);

        // When & Then
        mockMvc.perform(post("/api/stats/export")
                        .param("format", "json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(header().string("Content-Disposition", 
                        "attachment; filename=stats-" + TEST_SHORT_CODE + ".json"));

        verify(shortUrlService, times(1)).exportStats(anyString(), any(), any(), any(), any(), any(), eq("json"));
    }
}
