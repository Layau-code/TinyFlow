package com.layor.tinyflow.Controller;

import com.layor.tinyflow.entity.*;
import com.layor.tinyflow.dto.StatsQuery;
import com.layor.tinyflow.service.ShortUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 获取详细统计数据（包含所有维度）
     */
    @GetMapping("/detailed/{shortCode}")
    public ResponseEntity<DetailedStatsDTO> getDetailedStats(
            @PathVariable String shortCode,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        DetailedStatsDTO dto = shortUrlService.getDetailedStats(shortCode, start, end);
        return ResponseEntity.ok(dto);
    }

    /**
     * 获取全局统计数据
     */
    @GetMapping("/global")
    public ResponseEntity<GlobalStatsDTO> getGlobalStats(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        GlobalStatsDTO dto = shortUrlService.getGlobalStats(start, end);
        return ResponseEntity.ok(dto);
    }

    /**
     * 获取小时分布
     */
    @GetMapping("/hour/{shortCode}")
    public ResponseEntity<List<KeyCountDTO>> getHourDistribution(
            @PathVariable String shortCode,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        List<KeyCountDTO> list = shortUrlService.getHourDistribution(shortCode, start, end);
        return ResponseEntity.ok(list);
    }

    /**
     * 获取星期分布
     */
    @GetMapping("/weekday/{shortCode}")
    public ResponseEntity<List<KeyCountDTO>> getWeekdayDistribution(
            @PathVariable String shortCode,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        List<KeyCountDTO> list = shortUrlService.getWeekdayDistribution(shortCode, start, end);
        return ResponseEntity.ok(list);
    }

    /**
     * 获取浏览器分布
     */
    @GetMapping("/browser/{shortCode}")
    public ResponseEntity<List<KeyCountDTO>> getBrowserDistribution(
            @PathVariable String shortCode,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        List<KeyCountDTO> list = shortUrlService.getBrowserDistribution(shortCode, start, end);
        return ResponseEntity.ok(list);
    }

    /**
     * 获取国家分布
     */
    @GetMapping("/country/{shortCode}")
    public ResponseEntity<List<KeyCountDTO>> getCountryDistribution(
            @PathVariable String shortCode,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        List<KeyCountDTO> list = shortUrlService.getCountryDistribution(shortCode, start, end);
        return ResponseEntity.ok(list);
    }

    /**
     * 获取Referer详细分布
     */
    @GetMapping("/referer/{shortCode}")
    public ResponseEntity<List<KeyCountDTO>> getRefererDistribution(
            @PathVariable String shortCode,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        List<KeyCountDTO> list = shortUrlService.getRefererDistribution(shortCode, start, end);
        return ResponseEntity.ok(list);
    }

    /**
     * 获取PV/UV数据
     */
    @GetMapping("/pvuv/{shortCode}")
    public ResponseEntity<Map<String, Long>> getPvUv(
            @PathVariable String shortCode,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        Map<String, Long> data = shortUrlService.getPvUv(shortCode, start, end);
        return ResponseEntity.ok(data);
    }
}
