package com.layor.tinyflow.service;

import com.layor.tinyflow.Strategy.HashidsStrategy;
import com.layor.tinyflow.entity.*;
import com.layor.tinyflow.repository.DailyClickRepository;
import com.layor.tinyflow.repository.ShortUrlRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ShortUrlService {
    // 短码长度
    private static final int CODE_LENGTH = 6;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private DailyClickRepository dailyClickRepo;
    @Autowired
    private ShortUrlRepository shortUrlRepository;
    @Autowired
    private com.layor.tinyflow.repository.ClickEventRepository clickEventRepository;
    @Autowired
    private SegmentIdGenerator idGenerator;
    @Autowired
    HashidsStrategy codeStrategy;
    // 基础短链域名
    private static final String BASE_URL = "https://localhost:8080";




    public ShortUrlDTO createShortUrl(String longUrl, String customAlias) throws Exception {
        // 1. 校验长链接
        if (!isValidUrl(longUrl)) {
            throw new Exception("长链接格式不正确");
        }
        //1.1如果长链接已经存在，直接返回对应的短链
        if (shortUrlRepository.existsByLongUrl(longUrl)) {
            ShortUrl shortUrl = shortUrlRepository.findByLongUrl(longUrl);
            return ShortUrlDTO.builder()
                    .shortCode(shortUrl.getShortCode())
                    .shortUrl(BASE_URL + "/" + shortUrl.getShortCode())
                    .longUrl(shortUrl.getLongUrl())
                    .createdAt(shortUrl.getCreatedAt())
                    .build();
        }

        // 2. 处理别名
        String shortCode = customAlias;
        if (shortCode == null || shortCode.trim().isEmpty()) {
            // 自动生成随机码
            shortCode = generateRandomCode();
        } else {
            //用户自定义了别名
            shortUrlRepository.existsByShortCode(shortCode);
        }

        ShortUrl shortUrl = ShortUrl.builder()
                .longUrl(longUrl)
                .shortCode(shortCode)
                .createdAt(LocalDateTime.now())
                .build();

        // 3. 存入数据库
        shortUrlRepository.save(shortUrl);
        // 4. 构造返回结果
        ShortUrlDTO dto = ShortUrlDTO.builder()
                .shortCode(shortCode)
                .shortUrl(BASE_URL + "/" + shortCode)
                .longUrl(longUrl)
                .createdAt(LocalDateTime.now())
                .build();

        return dto;
    }

    private String generateRandomCode() {
        for (int i = 0; i < 3; i++) {
            long id = idGenerator.nextId("shorturl");
           String code = codeStrategy.encode(id);
            if (shortUrlRepository.existsByShortCode(code)) continue;
            if(code!=null) return code;
        }
       throw new IllegalStateException("GENERATE_FAILED");
    }

    private boolean isValidUrl(String url) {
        try {
            new URI(url).parseServerAuthority();
            return url != null && (url.startsWith("http://") || url.startsWith("https://"));
        } catch (URISyntaxException e) {
            return false;
        }
    }


    public String getLongUrlByShortCode(String shortCode) {
        //查询缓存
         String cachedLongUrl = redisTemplate.opsForValue().get("short_url:" + shortCode);
         if (cachedLongUrl != null) {
             return cachedLongUrl;
         }
        //缓存没有，查询数据库
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrl == null) {
            //数据库没有，返回null
            return null;
        }
        //更新缓存
        redisTemplate.opsForValue().set(
                "short_url:" + shortCode,
                shortUrl.getLongUrl(),
                Duration.ofHours(24) // 设置 24 小时过期
        );
        //返回长链接
        return shortUrl.getLongUrl();
    }

@Async
    public void recordClick(String shortCode) {
        LocalDate today = LocalDate.now();

        DailyClick dailyClick = dailyClickRepo.findByShortCodeAndDate(shortCode, today)
                .orElseGet(() -> DailyClick.builder()
                        .shortCode(shortCode)
                        .date(today)
                        .clicks(0)
                        .build());

        dailyClick.setClicks(dailyClick.getClicks() + 1);
        //url表也要更新
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode);
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        dailyClickRepo.save(dailyClick);
    }

    @Async
    public void recordClickEvent(String shortCode, HttpServletRequest request) {
        //提取请求相关信息
        String referer = request.getHeader("Referer");
        String ua = request.getHeader("User-Agent");
        String ip = extractIp(request);
        String host = extractHost(referer);
        String device = detectDevice(ua);
//        封装
        com.layor.tinyflow.entity.ClickEvent ev = com.layor.tinyflow.entity.ClickEvent.builder()
                .shortCode(shortCode)
                .ts(java.time.LocalDateTime.now())
                .referer(referer)
                .ua(ua)
                .ip(ip)
                .sourceHost(host)
                .deviceType(device)
                .city("")
                .country("")
                .build();
//        保存
        clickEventRepository.save(ev);
    }

    private String extractIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            int idx = xff.indexOf(',');
            return idx >= 0 ? xff.substring(0, idx).trim() : xff.trim();
        }
        String rip = req.getHeader("X-Real-IP");
        if (rip != null && !rip.isEmpty()) return rip.trim();
        return req.getRemoteAddr();
    }

    private String extractHost(String referer) {
        if (referer == null || referer.isEmpty()) return null;
        try { return new java.net.URI(referer).getHost(); } catch (Exception e) { return null; }
    }

    private String detectDevice(String ua) {
        String s = ua == null ? "" : ua.toLowerCase();
        if (s.contains("bot") || s.contains("spider") || s.contains("crawl")) return "bot";
        if (s.contains("ipad") || s.contains("tablet") || s.contains("pad")) return "tablet";
        if (s.contains("android") || s.contains("iphone") || s.contains("mobile")) return "mobile";
        return "desktop";
    }
    // UrlService.java
    public Page<UrlListResponseDTO> getAllUrls(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // 1. 先分页查出 ShortUrl 列表
        Page<ShortUrl> shortUrlPage = shortUrlRepository.findAll(pageable);
        List<ShortUrl> shortUrls = shortUrlPage.getContent();


        if (shortUrls.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<UrlListResponseDTO> urlListResponseDTO= shortUrls.stream()
                .map(shortUrl -> UrlListResponseDTO.builder()
                        .shortCode(shortUrl.getShortCode())
                        .longUrl(shortUrl.getLongUrl())
                        .totalVisits(Long.valueOf(shortUrl.getClickCount()))
                        .todayVisits(dailyClickRepo.getTodayClicksByShortCode(shortUrl.getShortCode()))
                        .createdAt(shortUrl.getCreatedAt())
                        .build()).toList();

        return new PageImpl<>(urlListResponseDTO, pageable, shortUrlPage.getTotalElements());
    
    }
    public ShortUrlOverviewDTO getOverviewByShortCode(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode);

        LocalDate today = LocalDate.now();

        int todayVisits = dailyClickRepo.findByShortCodeAndDate(shortCode, today)

                .map(DailyClick::getClicks)

                .orElse(0);

        return new ShortUrlOverviewDTO(
                shortUrl.getClickCount(),
                todayVisits,
                shortUrl.getCreatedAt().toInstant(ZoneOffset.UTC)
        );

    }
    // ShortUrlService.java
    public List<DailyVisitTrendDTO> getDailyTrendByShortCode(String shortCode, Integer days) {
        //1. 获取最近 N 天的日期
        List<LocalDate> dates = IntStream.range(0, days)
                .mapToObj(i -> LocalDate.now().minusDays(i))
                .toList();
        //2. 查询每个日期的点击次数
        List<DailyVisitTrendDTO> trendDTOs = dates.stream()
                .map(date -> {
                    Integer clicks = dailyClickRepo.findByShortCodeAndDate(shortCode, date)
                            .map(DailyClick::getClicks)
                            .orElse(0);
                    return new DailyVisitTrendDTO(date.toString(), clicks);
                })
                .toList();
        return trendDTOs;
    }


    /**
     * 获取短链接的访问分布统计信息
     * 
     * @param shortCode 短链接码
     * @param startStr 开始日期字符串，格式为 yyyy-MM-dd
     * @param endStr 结束日期字符串，格式为 yyyy-MM-dd
     * @param source 来源主机过滤条件
     * @param device 设备类型过滤条件
     * @param city 城市过滤条件
     * @return 包含来源、设备、城市分布统计的DistributionDTO对象
     */
    public DistributionDTO getDistribution(String shortCode, String startStr, String endStr, String source,String device,String city) {
        LocalDateTime end = parseEnd(endStr);
        LocalDateTime start = parseStart(startStr, end);
        List<Object[]> src = clickEventRepository.countBySource(shortCode, start, end, emptyToNull(source));
        List<Object[]> dev = clickEventRepository.countByDevice(shortCode, start, end, emptyToNull(device));
        List<Object[]> ct = clickEventRepository.countByCity(shortCode, start, end, emptyToNull(city));
        List<KeyCountDTO> referer = src.stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).toList();
        List<KeyCountDTO> deviceList = dev.stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).toList();
        List<KeyCountDTO> cityList = ct.stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).toList();
        return new DistributionDTO(referer, deviceList, cityList);
    }


    /**
     * 获取指定短链接的点击事件列表
     *
     * @param shortCode 短链接码
     * @param startStr  开始日期字符串，格式为 yyyy-MM-dd
     * @param endStr    结束日期字符串，格式为 yyyy-MM-dd
     * @param source    来源主机过滤条件
     * @param device    设备类型过滤条件
     * @param city      城市过滤条件
     * @param page      页码（从0开始）
     * @param size      每页大小
     * @return 点击事件列表
     */
    public List<ClickEventDTO> getEvents(String shortCode, String startStr, String endStr, String source, String device, String city, Integer page, Integer size) {
        LocalDateTime end = parseEnd(endStr);
        LocalDateTime start = parseStart(startStr, end);
        Pageable pageable = PageRequest.of(Math.max(0, page == null ? 0 : page), Math.max(1, size == null ? 20 : size));
        Page<com.layor.tinyflow.entity.ClickEvent> p = clickEventRepository.findEvents(shortCode, start, end, emptyToNull(source), emptyToNull(device), emptyToNull(city), pageable);
        return p.getContent().stream().map(e -> new ClickEventDTO(e.getTs(), e.getIp(), e.getSourceHost(), e.getDeviceType(), e.getCity(), e.getCountry(), e.getUa(), e.getReferer())).toList();
    }

    public byte[] exportStats(String shortCode, String startStr, String endStr, String source, String device, String city, String format) {
        LocalDateTime end = parseEnd(endStr);
        LocalDateTime start = parseStart(startStr, end);
        List<com.layor.tinyflow.entity.ClickEvent> list = clickEventRepository.findEvents(shortCode, start, end, emptyToNull(source), emptyToNull(device), emptyToNull(city), PageRequest.of(0, 100000)).getContent();
        if ("json".equalsIgnoreCase(format)) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i=0;i<list.size();i++) {
                var e = list.get(i);
                sb.append("{")
                        .append("\"ts\":\"").append(e.getTs()).append("\",")
                        .append("\"ip\":\"").append(nullToEmpty(e.getIp())).append("\",")
                        .append("\"source_host\":\"").append(nullToEmpty(e.getSourceHost())).append("\",")
                        .append("\"device_type\":\"").append(nullToEmpty(e.getDeviceType())).append("\",")
                        .append("\"city\":\"").append(nullToEmpty(e.getCity())).append("\",")
                        .append("\"country\":\"").append(nullToEmpty(e.getCountry())).append("\",")
                        .append("\"ua\":\"").append(escapeJson(nullToEmpty(e.getUa()))).append("\",")
                        .append("\"referer\":\"").append(escapeJson(nullToEmpty(e.getReferer()))).append("\"}");
                if (i < list.size()-1) sb.append(",");
            }
            sb.append("]");
            return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ts,ip,source_host,device_type,city,country,ua,referer\n");
        for (var e : list) {
            sb.append(e.getTs()).append(',')
              .append(csv(nullToEmpty(e.getIp()))).append(',')
              .append(csv(nullToEmpty(e.getSourceHost()))).append(',')
              .append(csv(nullToEmpty(e.getDeviceType()))).append(',')
              .append(csv(nullToEmpty(e.getCity()))).append(',')
              .append(csv(nullToEmpty(e.getCountry()))).append(',')
              .append(csv(nullToEmpty(e.getUa()))).append(',')
              .append(csv(nullToEmpty(e.getReferer()))).append('\n');
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private LocalDateTime parseEnd(String endStr) {
        if (endStr == null || endStr.isEmpty()) return LocalDateTime.now();
        try { return LocalDate.parse(endStr).atTime(23,59,59); } catch (Exception e) { return LocalDateTime.now(); }
    }

    private LocalDateTime parseStart(String startStr, LocalDateTime end) {
        if (startStr == null || startStr.isEmpty()) return end.minusDays(7);
        try { return LocalDate.parse(startStr).atStartOfDay(); } catch (Exception e) { return end.minusDays(7); }
    }

    private String emptyToNull(String s) { return (s == null || s.isEmpty()) ? null : s; }
    private long n(Object o) { return o == null ? 0L : ((Number)o).longValue(); }
    private String s(Object o) { return o == null ? "" : String.valueOf(o); }
    private String csv(String s) { String t = s.replace("\"", "\"\""); return '"' + t + '"'; }
    private String nullToEmpty(String s) { return s == null ? "" : s; }
    private String escapeJson(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }
    @Transactional
    public void deleteByShortCode(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrl == null) {
            throw new NoSuchElementException("Short URL not found");
        }
        shortUrlRepository.deleteByShortCode(shortCode);

        dailyClickRepo.deleteByShortCode(shortCode);
    }

    public List<UrlClickStatsDTO> getUrlClickStats() {
        return shortUrlRepository.findAll().stream()
                .map(shortUrl -> new UrlClickStatsDTO(
                        shortUrl.getShortCode(),
                        shortUrl.getClickCount(),
                        dailyClickRepo.getTodayClicksByShortCode(shortUrl.getShortCode())
                ))
                .toList();
    }

    public Map<String, List<DailyVisitTrendDTO>> getVisitTrendsByShortCodes(List<String> shortCodes, Integer days) {
        return shortCodes.stream()
                .collect(Collectors.toMap(
                        shortCode -> shortCode,
                        shortCode -> getDailyTrendByShortCode(shortCode, days)
                ));
    }


    @Transactional
    public void updateShortUrl(String shortCode,  String customAlias) {
        //1. 查询短链是否存在
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrl == null) {
            throw new RuntimeException("短链不存在");
        }
        if (customAlias != null && !customAlias.isEmpty()) {
            //2. 检查自定义别名是否已存在
            if (shortUrlRepository.existsByShortCode(customAlias)) {
                throw new RuntimeException("自定义别名已存在");
            }
            //3. 更新短链的别名
            shortUrl.setShortCode(customAlias);
        }
        // 更新数据库url表
        shortUrlRepository.save(shortUrl);
        
        //更新数据dailyclick表
        DailyClick dailyClick = dailyClickRepo.findByShortCode(shortCode);
        if(dailyClick != null){
       dailyClick.setShortCode(customAlias);
        dailyClickRepo.save(dailyClick);}


        // 更新缓存
        redisTemplate.delete("short_url:" + shortCode);
        redisTemplate.opsForValue().set(
                "short_url:" + customAlias,
                shortUrl.getLongUrl(),
                Duration.ofHours(24)
        );

    }
//短链跳转
    public void redirectCode(String code, HttpServletRequest request, HttpServletResponse response) {
        ShortUrl shortCode = shortUrlRepository.findByShortCode(code);
        if (shortCode == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String longUrl = shortCode.getLongUrl();
        recordClick(code);
        try { recordClickEvent(code, request); } catch (Exception ignored) {}
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", longUrl);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    }
}
