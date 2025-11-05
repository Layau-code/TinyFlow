package com.layor.tinyflow.Controller;

import com.layor.tinyflow.entity.DailyVisitTrendDTO;
import com.layor.tinyflow.entity.ShortUrlOverviewDTO;
import com.layor.tinyflow.service.ShortUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}