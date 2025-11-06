package com.layor.tinyflow.Controller;

import com.layor.tinyflow.entity.DailyVisitTrendDTO;
import com.layor.tinyflow.entity.ShortUrlOverviewDTO;
import com.layor.tinyflow.service.ShortUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final ShortUrlService shortUrlService;

    @GetMapping("/overview/{shortCode}")
    public ResponseEntity<ShortUrlOverviewDTO> getShortUrlOverview(
            @PathVariable String shortCode) {
        ShortUrlOverviewDTO dto = shortUrlService.getOverviewByShortCode(shortCode);
        return ResponseEntity.ok(dto);
    }
    @GetMapping("/trend/{shortCode}")
    public ResponseEntity<List<DailyVisitTrendDTO>> getTrend(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "7") Integer days) {

        List<DailyVisitTrendDTO> trend = shortUrlService.getDailyTrendByShortCode(shortCode, days);
        return ResponseEntity.ok(trend);
    }

    @GetMapping("/compare")
//后续扩展30天...
    public Map<String, List<DailyVisitTrendDTO>> compareTrends(@RequestParam String trends, @RequestParam(defaultValue = "7") Integer days) {
        // 参数校验
        if (trends == null || trends.trim().isEmpty()) {
            throw new IllegalArgumentException("短码列表不能为空");
        }
        List<String> shortCodes = Arrays.asList(trends.split(","));
        return shortUrlService.getVisitTrendsByShortCodes(shortCodes, days);
    }
}