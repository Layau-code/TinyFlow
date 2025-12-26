package com.layor.tinyflow.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.layor.tinyflow.Strategy.HashidsStrategy;
import com.layor.tinyflow.entity.*;
import com.layor.tinyflow.repository.DailyClickRepository;
import com.layor.tinyflow.repository.ShortUrlRepository;
import com.layor.tinyflow.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class ShortUrlService {
    // 短码长度
    private static final int CODE_LENGTH = 6;
    
    @Autowired
    @Qualifier("localUrlCache")
    private Cache<String, String> localCache;
    
    @Value("${cache.warmup.enabled:true}")
    private boolean warmupEnabled;
    
    @Value("${cache.warmup.size:1000}")
    private int warmupSize;
    
    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    @Autowired
    private DailyClickRepository dailyClickRepo;
    @Autowired
    private ShortUrlRepository shortUrlRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private com.layor.tinyflow.repository.ClickEventRepository clickEventRepository;
    @Autowired
    private SegmentIdGenerator idGenerator;
    @Autowired
    HashidsStrategy codeStrategy;
    @Autowired
    private AuthService authService; // 注入认证服务
    
    @Value("${APP_DOMAIN:http://localhost:8080}")
    private String baseUrl;
    
    public ShortUrlDTO createShortUrl(String longUrl, String customAlias) throws Exception {
        // 1. 校验长链接
        if (!isValidUrl(longUrl)) {
            throw new Exception("长链接格式不正确");
        }
        
        // 获取当前登录用户ID（如果未登录则为null）
        Long userId = authService.getCurrentUserId();
        
        // 如果未登录，使用默认用户 "user" 的ID
        if (userId == null) {
            User defaultUser = userRepository.findByUsername("user").orElse(null);
            if (defaultUser != null) {
                userId = defaultUser.getId();
            }
        }
        
        //1.1如果长链接已经存在且属于当前用户，直接返回对应的短链
        ShortUrl existingUrl = null;
        if (userId != null) {
            // 已登录或使用默认用户：查询当前用户的短链
            existingUrl = shortUrlRepository.findByUserIdAndLongUrl(userId, longUrl);
        } else {
            // 匿名用户：查询匿名短链
            existingUrl = shortUrlRepository.findByUserIdIsNullAndLongUrl(longUrl);
        }
        
        if (existingUrl != null) {
            return ShortUrlDTO.builder()
                    .shortCode(existingUrl.getShortCode())
                    .shortUrl(baseUrl + "/" + existingUrl.getShortCode())
                    .longUrl(existingUrl.getLongUrl())
                    .createdAt(existingUrl.getCreatedAt())
                    .build();
        }

        // 2. 处理别名
        String shortCode = customAlias;
        if (shortCode == null || shortCode.trim().isEmpty()) {
            // 自动生成随机码
            shortCode = generateRandomCode();
        } else {
            //用户自定义了别名，检查是否已存在
            if (shortUrlRepository.existsByShortCode(shortCode)) {
                throw new Exception("自定义别名已存在");
            }
        }

        ShortUrl shortUrl = ShortUrl.builder()
                .longUrl(longUrl)
                .shortCode(shortCode)
                .userId(userId) // 设置创建者ID（可能为默认用户ID或登录用户ID）
                .createdAt(LocalDateTime.now())
                .build();

        // 3. 存入数据库
        shortUrlRepository.save(shortUrl);
        log.info("用户 {} 创建短链: {} -> {}", userId, shortCode, longUrl);
        
        // 4. 构造返回结果
        ShortUrlDTO dto = ShortUrlDTO.builder()
                .shortCode(shortCode)
                .shortUrl(baseUrl + "/" + shortCode)
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

    /**
     * 启动时预热缓存：加载热点短链到本地 Caffeine 缓存
     */
    @PostConstruct
    public void warmupCache() {
        if (!warmupEnabled) {
            log.info("Cache warmup disabled");
            return;
        }
        
        try {
            log.info("Starting cache warmup, loading top {} hot URLs...", warmupSize);
            Pageable topN = PageRequest.of(0, warmupSize);
            Page<ShortUrl> hotUrls = shortUrlRepository.findAll(topN);
            
            int l1Loaded = 0, l2Loaded = 0;
            for (ShortUrl url : hotUrls.getContent()) {
                String shortCode = url.getShortCode();
                String longUrl = url.getLongUrl();
                
                // 填充L1本地缓存
                localCache.put(shortCode, longUrl);
                l1Loaded++;
                
                // 同时填充L2 Redis缓存
                try {
                    redisTemplate.opsForValue().set(
                        "short_url:" + shortCode, 
                        longUrl, 
                        java.time.Duration.ofHours(24)
                    );
                    l2Loaded++;
                } catch (Exception e) {
                    log.warn("Redis warmup failed for {}: {}", shortCode, e.getMessage());
                }
            }
            
            log.info("Cache warmup completed: L1(本地)={}条, L2(Redis)={}条", l1Loaded, l2Loaded);
        } catch (Exception e) {
            log.error("Cache warmup failed: {}", e.getMessage(), e);
        }
    }



    @Autowired
    private ClickRecorderService clickRecorderService;

    public void recordClick(String shortCode) {
        clickRecorderService.recordClick(shortCode);
    }

    public void recordClickEvent(String shortCode, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        String ua = request.getHeader("User-Agent");
        String ip = extractIp(request);
        String host = extractHost(referer);
        String device = detectDevice(ua);
        clickRecorderService.recordClickEvent(shortCode, referer, ua, ip, host, device);
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
        // 获取当前登录用户ID（如果未登录则为null）
        Long userId = authService.getCurrentUserId();
        
        Pageable pageable = PageRequest.of(page, size);

        // 1. 如果用户已登录，只查询当前用户的短链接；否则查询所有匿名短链
        Page<ShortUrl> shortUrlPage;
        if (userId != null) {
            // 已登录：查询自己的短链
            shortUrlPage = shortUrlRepository.findByUserId(userId, pageable);
        } else {
            // 未登录：查询默认用户"user"的短链
            User defaultUser = userRepository.findByUsername("user").orElse(null);
            if (defaultUser != null) {
                shortUrlPage = shortUrlRepository.findByUserId(defaultUser.getId(), pageable);
            } else {
                // 如果"user"不存在，返回空列表
                shortUrlPage = shortUrlRepository.findByUserIdIsNull(pageable);
            }
        }
        List<ShortUrl> shortUrls = shortUrlPage.getContent();


        if (shortUrls.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<String> codes = shortUrls.stream().map(ShortUrl::getShortCode).toList();
        Map<String, Integer> todayMap = dailyClickRepo.findTodayClicksByShortCodes(codes).stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).intValue()));
        List<UrlListResponseDTO> urlListResponseDTO = shortUrls.stream()
                .map(shortUrl -> UrlListResponseDTO.builder()
                        .shortCode(shortUrl.getShortCode())
                        .longUrl(shortUrl.getLongUrl())
                        .totalVisits(Long.valueOf(shortUrl.getClickCount()))
                        .todayVisits(todayMap.getOrDefault(shortUrl.getShortCode(), 0))
                        .createdAt(shortUrl.getCreatedAt())
                        .build())
                .toList();

        return new PageImpl<>(urlListResponseDTO, pageable, shortUrlPage.getTotalElements());
    
    }
    @io.github.resilience4j.ratelimiter.annotation.RateLimiter(name = "redirectLimit")
    public void redirectCode(String code, HttpServletRequest request, HttpServletResponse response) {
        String longUrl = getLongUrlByShortCode(code);
        if (longUrl == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        recordClick(code);
        recordClickEvent(code, request);
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", longUrl);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    }

    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "redisBreaker", fallbackMethod = "redisFallback")
    @io.github.resilience4j.retry.annotation.Retry(name = "redisRetry")
    public String getLongUrlByShortCode(String shortCode) {
        long startTime = System.currentTimeMillis();
        
        // L1: 本地 Caffeine 缓存
        String cachedUrl = localCache.getIfPresent(shortCode);
        if (cachedUrl != null) {
            log.debug("[L1 HIT] shortCode={}, duration={}ms", shortCode, System.currentTimeMillis() - startTime);
            return cachedUrl;
        }
        
        // L2: Redis 缓存
        String redisUrl = null;
        try { 
            redisUrl = redisTemplate.opsForValue().get("short_url:" + shortCode);
            log.debug("[L2 QUERY] shortCode={}, hit={}, duration={}ms", 
                shortCode, redisUrl != null, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.warn("[L2 ERROR] Redis query failed for {}: {}", shortCode, e.getMessage());
            throw e; // 抛出异常触发熔断器
        }
        
        if (redisUrl != null) {
            // 回填 L1
            localCache.put(shortCode, redisUrl);
            return redisUrl;
        }

        // L3: 数据库回源
        log.info("[L3 QUERY] Database fallback for shortCode={}", shortCode);
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrl == null) {
            log.warn("[NOT FOUND] shortCode={}", shortCode);
            return null;
        }
        
        String longUrl = shortUrl.getLongUrl();
        
        // 回填 Redis (L2)
        try {
            redisTemplate.opsForValue().set("short_url:" + shortCode, longUrl, java.time.Duration.ofHours(24));
        } catch (Exception e) {
            log.warn("[L2 ERROR] Redis set failed for {}: {}", shortCode, e.getMessage());
        }
        
        // 回填本地缓存 (L1)
        localCache.put(shortCode, longUrl);
        
        log.info("[DB HIT] shortCode={}, totalDuration={}ms", shortCode, System.currentTimeMillis() - startTime);
        return longUrl;
    }

    public String redisFallback(String shortCode, Throwable t) {
        log.error("[FALLBACK] Redis circuit breaker triggered for shortCode={}, reason={}", 
            shortCode, t.getMessage());
        // 降级逻辑：Redis 挂了直接查数据库
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrl != null) {
            // 回填本地缓存
            localCache.put(shortCode, shortUrl.getLongUrl());
            return shortUrl.getLongUrl();
        }
        return null;
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

    public DistributionDTO getDistribution(String shortCode, com.layor.tinyflow.dto.StatsQuery q) {
        LocalDateTime end = parseEnd(q.getEnd());
        LocalDateTime start = parseStart(q.getStart(), end);
        List<Object[]> src = clickEventRepository.countBySource(shortCode, start, end, emptyToNull(q.getSource()));
        List<Object[]> dev = clickEventRepository.countByDevice(shortCode, start, end, emptyToNull(q.getDevice()));
        List<Object[]> ct = clickEventRepository.countByCity(shortCode, start, end, emptyToNull(q.getCity()));
        List<KeyCountDTO> referer = src.stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).toList();
        List<KeyCountDTO> deviceList = dev.stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).toList();
        List<KeyCountDTO> cityList = ct.stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).toList();
        return new DistributionDTO(referer, deviceList, cityList);
    }

    // Backward-compatible overloads to satisfy existing callers
    public DistributionDTO getDistribution(String shortCode, String startStr, String endStr, String source, String device, String city, Integer page, Integer size) {
        com.layor.tinyflow.dto.StatsQuery q = new com.layor.tinyflow.dto.StatsQuery();
        q.setStart(startStr); q.setEnd(endStr); q.setSource(source); q.setDevice(device); q.setCity(city); q.setPage(page); q.setSize(size);
        return getDistribution(shortCode, q);
    }

    public List<ClickEventDTO> getEvents(String shortCode, com.layor.tinyflow.dto.StatsQuery q) {
        LocalDateTime end = parseEnd(q.getEnd());
        LocalDateTime start = parseStart(q.getStart(), end);
        Pageable pageable = PageRequest.of(Math.max(0, q.getPage() == null ? 0 : q.getPage()), Math.max(1, q.getSize() == null ? 20 : q.getSize()));
        Page<com.layor.tinyflow.entity.ClickEvent> p = clickEventRepository.findEvents(shortCode, start, end, emptyToNull(q.getSource()), emptyToNull(q.getDevice()), emptyToNull(q.getCity()), pageable);
        return p.getContent().stream().map(e -> new ClickEventDTO(e.getTs(), e.getIp(), e.getSourceHost(), e.getDeviceType(), e.getCity(), e.getCountry(), e.getUa(), e.getReferer())).toList();
    }

    // Backward-compatible overload
    public List<ClickEventDTO> getEvents(String shortCode, String startStr, String endStr, String source, String device, String city, Integer page, Integer size) {
        com.layor.tinyflow.dto.StatsQuery q = new com.layor.tinyflow.dto.StatsQuery();
        q.setStart(startStr); q.setEnd(endStr); q.setSource(source); q.setDevice(device); q.setCity(city); q.setPage(page); q.setSize(size);
        return getEvents(shortCode, q);
    }

    public byte[] exportStats(String shortCode, com.layor.tinyflow.dto.StatsQuery q, String format) {
        LocalDateTime end = parseEnd(q.getEnd());
        LocalDateTime start = parseStart(q.getStart(), end);
        List<com.layor.tinyflow.entity.ClickEvent> list = clickEventRepository.findEvents(shortCode, start, end, emptyToNull(q.getSource()), emptyToNull(q.getDevice()), emptyToNull(q.getCity()), PageRequest.of(0, 100000)).getContent();
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

    // Backward-compatible overload
    public byte[] exportStats(String shortCode, String startStr, String endStr, String source, String device, String city, String format) {
        com.layor.tinyflow.dto.StatsQuery q = new com.layor.tinyflow.dto.StatsQuery();
        q.setStart(startStr); q.setEnd(endStr); q.setSource(source); q.setDevice(device); q.setCity(city); q.setPage(0); q.setSize(100000);
        return exportStats(shortCode, q, format);
    }

    private LocalDateTime parseEnd(String endStr) {
        if (endStr == null || endStr.isEmpty()) {
            LocalDate mEnd = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
            return mEnd.atTime(23, 59, 59);
        }
        try { return LocalDate.parse(endStr).atTime(23,59,59); } catch (Exception e) {
            LocalDate mEnd = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
            return mEnd.atTime(23, 59, 59);
        }
    }

    private LocalDateTime parseStart(String startStr, LocalDateTime end) {
        if (startStr == null || startStr.isEmpty()) {
            LocalDate mStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            return mStart.atStartOfDay();
        }
        try { return LocalDate.parse(startStr).atStartOfDay(); } catch (Exception e) {
            LocalDate mStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            return mStart.atStartOfDay();
        }
    }

    private String emptyToNull(String s) { return (s == null || s.isEmpty()) ? null : s; }
    private long n(Object o) { return o == null ? 0L : ((Number)o).longValue(); }
    private String s(Object o) { return o == null ? "" : String.valueOf(o); }
    private String csv(String s) { String t = s.replace("\"", "\"\""); return '"' + t + '"'; }
    private String nullToEmpty(String s) { return s == null ? "" : s; }
    private String escapeJson(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }
    @Transactional
    public void deleteByShortCode(String shortCode) {
        // 获取当前登录用户ID（如果未登录则为null）
        Long userId = authService.getCurrentUserId();
        
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrl == null) {
            throw new NoSuchElementException("Short URL not found");
        }
        
        // 权限检查：只能删除自己的短链（匿名用户只能删除匿名短链）
        if (userId != null && shortUrl.getUserId() != null && !shortUrl.getUserId().equals(userId)) {
            throw new SecurityException("无权删除此短链接");
        }
        if (userId == null && shortUrl.getUserId() != null) {
            throw new SecurityException("无权删除此短链接");
        }
        
        shortUrlRepository.deleteByShortCode(shortCode);
        dailyClickRepo.deleteByShortCode(shortCode);
        log.info("用户 {} 删除短链: {}", userId, shortCode);
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

    /**
     * 获取详细统计数据
     */
    public DetailedStatsDTO getDetailedStats(String shortCode, String startStr, String endStr) {
        LocalDateTime end = parseEnd(endStr);
        LocalDateTime start = parseStart(startStr, end);
        
        // 基础指标
        long pv = clickEventRepository.countTotal(shortCode, start, end);
        long uv = clickEventRepository.countUniqueIp(shortCode, start, end);
        double pvUvRatio = uv > 0 ? (double) pv / uv : 0;
        
        // 时间分布
        List<KeyCountDTO> hourDist = clickEventRepository.countByHour(shortCode, start, end)
                .stream().map(o -> new KeyCountDTO(formatHour(n(o[0])), n(o[1]))).toList();
        List<KeyCountDTO> weekdayDist = clickEventRepository.countByDayOfWeek(shortCode, start, end)
                .stream().map(o -> new KeyCountDTO(formatWeekday((int)n(o[0])), n(o[1]))).toList();
        
        // 地理分布
        List<KeyCountDTO> countryDist = clickEventRepository.countByCountry(shortCode, start, end)
                .stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).limit(20).toList();
        List<KeyCountDTO> cityDist = clickEventRepository.countByCity(shortCode, start, end, null)
                .stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).limit(20).toList();
        
        // 技术分布
        List<KeyCountDTO> deviceDist = clickEventRepository.countByDevice(shortCode, start, end, null)
                .stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).toList();
        List<KeyCountDTO> browserDist = clickEventRepository.countByUa(shortCode, start, end)
                .stream().map(o -> new KeyCountDTO(parseBrowser(s(o[0])), n(o[1]))).limit(10).toList();
        
        // 来源分布
        List<KeyCountDTO> sourceDist = clickEventRepository.countBySource(shortCode, start, end, null)
                .stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).limit(20).toList();
        List<KeyCountDTO> refererDist = clickEventRepository.countByReferer(shortCode, start, end)
                .stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).limit(20).toList();
        
        // 时间信息
        LocalDateTime firstClick = clickEventRepository.findFirstClickTime(shortCode);
        LocalDateTime lastClick = clickEventRepository.findLastClickTime(shortCode);
        
        return new DetailedStatsDTO(pv, uv, pvUvRatio, hourDist, weekdayDist, 
                countryDist, cityDist, deviceDist, browserDist, sourceDist, refererDist, 
                firstClick, lastClick);
    }

    /**
     * 获取全局统计数据
     */
    public GlobalStatsDTO getGlobalStats(String startStr, String endStr) {
        LocalDateTime end = parseEnd(endStr);
        LocalDateTime start = parseStart(startStr, end);
        
        // 基础汇总
        long totalUrls = shortUrlRepository.count();
        long totalClicks = clickEventRepository.countAllTotal(start, end);
        long totalUv = clickEventRepository.countAllUniqueIp(start, end);
        
        // 今日数据
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        long todayClicks = clickEventRepository.countAllTotal(todayStart, todayEnd);
        
        // 活跃短链
        List<String> todayActiveCodes = dailyClickRepo.findTodayActiveCodes();
        long activeUrls = todayActiveCodes != null ? todayActiveCodes.size() : 0;
        
        // 日趋势
        List<KeyCountDTO> dailyTrend = clickEventRepository.countAllByDate(start, end)
                .stream().map(o -> new KeyCountDTO(String.valueOf(o[0]), n(o[1]))).toList();
        
        // 分布数据
        List<KeyCountDTO> deviceDist = clickEventRepository.countAllByDevice(start, end)
                .stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).toList();
        List<KeyCountDTO> cityTop10 = clickEventRepository.countAllByCity(start, end)
                .stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).limit(10).toList();
        List<KeyCountDTO> sourceTop10 = clickEventRepository.countAllBySource(start, end)
                .stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).limit(10).toList();
        
        // 热门短链 TOP10
        List<UrlRankDTO> topUrls = shortUrlRepository.findAll(PageRequest.of(0, 10, 
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "clickCount")))
                .stream()
                .map(url -> {
                    Integer urlTodayClicks = dailyClickRepo.getTodayClicksByShortCode(url.getShortCode());
                    int today = urlTodayClicks != null ? urlTodayClicks : 0;
                    return new UrlRankDTO(url.getShortCode(), url.getLongUrl(), url.getClickCount(), today);
                })
                .toList();
        
        return new GlobalStatsDTO(totalUrls, totalClicks, totalUv, todayClicks, activeUrls,
                dailyTrend, deviceDist, cityTop10, sourceTop10, topUrls);
    }

    /**
     * 获取小时分布数据
     */
    public List<KeyCountDTO> getHourDistribution(String shortCode, String startStr, String endStr) {
        LocalDateTime end = parseEnd(endStr);
        LocalDateTime start = parseStart(startStr, end);
        return clickEventRepository.countByHour(shortCode, start, end)
                .stream().map(o -> new KeyCountDTO(formatHour(n(o[0])), n(o[1]))).toList();
    }

    /**
     * 获取星期分布数据
     */
    public List<KeyCountDTO> getWeekdayDistribution(String shortCode, String startStr, String endStr) {
        LocalDateTime end = parseEnd(endStr);
        LocalDateTime start = parseStart(startStr, end);
        return clickEventRepository.countByDayOfWeek(shortCode, start, end)
                .stream().map(o -> new KeyCountDTO(formatWeekday((int)n(o[0])), n(o[1]))).toList();
    }

    /**
     * 获取浏览器分布数据
     */
    public List<KeyCountDTO> getBrowserDistribution(String shortCode, String startStr, String endStr) {
        LocalDateTime end = parseEnd(endStr);
        LocalDateTime start = parseStart(startStr, end);
        // 合并相同浏览器
        Map<String, Long> browserMap = new HashMap<>();
        clickEventRepository.countByUa(shortCode, start, end).forEach(o -> {
            String browser = parseBrowser(s(o[0]));
            browserMap.merge(browser, n(o[1]), Long::sum);
        });
        return browserMap.entrySet().stream()
                .map(e -> new KeyCountDTO(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .limit(10).toList();
    }

    /**
     * 获取国家分布数据
     */
    public List<KeyCountDTO> getCountryDistribution(String shortCode, String startStr, String endStr) {
        LocalDateTime end = parseEnd(endStr);
        LocalDateTime start = parseStart(startStr, end);
        return clickEventRepository.countByCountry(shortCode, start, end)
                .stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).limit(20).toList();
    }

    /**
     * 获取Referer详细分布
     */
    public List<KeyCountDTO> getRefererDistribution(String shortCode, String startStr, String endStr) {
        LocalDateTime end = parseEnd(endStr);
        LocalDateTime start = parseStart(startStr, end);
        return clickEventRepository.countByReferer(shortCode, start, end)
                .stream().map(o -> new KeyCountDTO(s(o[0]), n(o[1]))).limit(50).toList();
    }

    /**
     * 获取PV/UV数据
     */
    public Map<String, Long> getPvUv(String shortCode, String startStr, String endStr) {
        LocalDateTime end = parseEnd(endStr);
        LocalDateTime start = parseStart(startStr, end);
        long pv = clickEventRepository.countTotal(shortCode, start, end);
        long uv = clickEventRepository.countUniqueIp(shortCode, start, end);
        Map<String, Long> result = new HashMap<>();
        result.put("pv", pv);
        result.put("uv", uv);
        return result;
    }

    private String formatHour(long hour) {
        return String.format("%02d:00", hour);
    }

    private String formatWeekday(int dayOfWeek) {
        String[] days = {"", "周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        return dayOfWeek >= 1 && dayOfWeek <= 7 ? days[dayOfWeek] : "";
    }

    private String parseBrowser(String ua) {
        if (ua == null || ua.isEmpty()) return "未知";
        String s = ua.toLowerCase();
        if (s.contains("edg")) return "Edge";
        if (s.contains("chrome") && !s.contains("edg")) return "Chrome";
        if (s.contains("safari") && !s.contains("chrome")) return "Safari";
        if (s.contains("firefox")) return "Firefox";
        if (s.contains("opera") || s.contains("opr")) return "Opera";
        if (s.contains("msie") || s.contains("trident")) return "IE";
        return "其他";
    }

    @Transactional
    public void updateShortUrl(String shortCode,  String customAlias) {
        // 获取当前登录用户ID（如果未登录则为null）
        Long userId = authService.getCurrentUserId();
        
        //1. 查询短链是否存在
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrl == null) {
            throw new RuntimeException("短链不存在");
        }
        
        // 权限检查：只能修改自己的短链（匿名用户只能修改匿名短链）
        if (userId != null && shortUrl.getUserId() != null && !shortUrl.getUserId().equals(userId)) {
            throw new SecurityException("无权修改此短链接");
        }
        if (userId == null && shortUrl.getUserId() != null) {
            throw new SecurityException("无权修改此短链接");
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


        // 清理缓存
        localCache.invalidate(shortCode);
        try {
            redisTemplate.delete("short_url:" + shortCode);
            redisTemplate.opsForValue().set(
                    "short_url:" + customAlias,
                    shortUrl.getLongUrl(),
                    Duration.ofHours(24)
            );
        } catch (Exception e) {
            log.warn("Cache update failed: {}", e.getMessage());
        }
        
        log.info("用户 {} 修改短链: {} -> {}", userId, shortCode, customAlias);
    }
}
