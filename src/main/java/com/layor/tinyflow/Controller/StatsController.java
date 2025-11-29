package com.layor.tinyflow.Controller;

import com.layor.tinyflow.entity.DailyVisitTrendDTO;
import com.layor.tinyflow.entity.DistributionDTO;
import com.layor.tinyflow.entity.ClickEventDTO;
import com.layor.tinyflow.entity.ShortUrlOverviewDTO;
import com.layor.tinyflow.dto.StatsQuery;
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
        List<String> shortCodes = List.of(trends.split(","));
        return shortUrlService.getVisitTrendsByShortCodes(shortCodes, days);
    }

    /**
     * 获取指定短链接的访问分布统计信息
     * 
     * @param q 统计查询参数对象，包含短链接码、时间范围、来源、设备、城市等筛选条件
     * @return 包含访问分布数据的响应实体
     */
    @PostMapping("/distribution")
    public ResponseEntity<DistributionDTO> getDistribution(
            @RequestBody StatsQuery q) {
        DistributionDTO dto = shortUrlService.getDistribution(q.getCode(), q.getStart(), q.getEnd(), q.getSource(), q.getDevice(), q.getCity(), q.getPage(), q.getSize());
        return ResponseEntity.ok(dto);
    }

    /**
     * 获取指定短链接的点击事件详情列表
     * 
     * @param q 统计查询参数对象，包含短链接码、时间范围、来源、设备、城市、分页等筛选条件
     * @return 包含点击事件详情列表的响应实体
     */
    @PostMapping("/events")
    public ResponseEntity<List<ClickEventDTO>> getEvents(
            @RequestBody StatsQuery q) {
        List<ClickEventDTO> list = shortUrlService.getEvents( q.getCode(), q.getStart(), q.getEnd(), q.getSource(), q.getDevice(), q.getCity(), q.getPage(), q.getSize());
        return ResponseEntity.ok(list);
    }
    

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestBody StatsQuery q,
            @RequestParam(defaultValue = "csv") String format) {
        byte[] bytes = shortUrlService.exportStats(q.getCode(), q.getStart(), q.getEnd(), q.getSource(), q.getDevice(), q.getCity(), format);
        String ct = "csv".equalsIgnoreCase(format) ? "text/csv" : "application/json";
        return ResponseEntity.ok()
                .header("Content-Type", ct)
                .header("Content-Disposition", "attachment; filename=stats-" + q.getCode() + "." + ("csv".equalsIgnoreCase(format) ? "csv" : "json"))
                .body(bytes);
    }
}
