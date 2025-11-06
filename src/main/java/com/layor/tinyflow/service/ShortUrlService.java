package com.layor.tinyflow.service;

import com.layor.tinyflow.entity.*;
import com.layor.tinyflow.repository.DailyClickRepository;
import com.layor.tinyflow.repository.ShortUrlRepository;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class ShortUrlService {
    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    // 短码长度
    private static final int CODE_LENGTH = 6;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private DailyClickRepository dailyClickRepo;
    @Autowired
    private ShortUrlRepository shortUrlRepository;
    // 基础短链域名
    private static final String BASE_URL = "https://localhost:8080";


    public ShortUrlDTO createShortUrl(String longUrl, String customAlias) throws Exception {
        // 1. 校验长链接
        if (!isValidUrl(longUrl)) {
            throw new Exception("长链接格式不正确");
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
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = (int) (Math.random() * characters.length());
            code.append(characters.charAt(index));
        }
        return code.toString();
    }

    private boolean isValidUrl(String url) {
        try {
            new URI(url).parseServerAuthority();
            return url != null && (url.startsWith("http://") || url.startsWith("https://"));
        } catch (URISyntaxException e) {
            return false;
        }
    }

@Transactional
    public String getLongUrlByShortCode(String shortCode) {

        //查询缓存
         String cachedLongUrl = redisTemplate.opsForValue().get("short_url:" + shortCode);
         if (cachedLongUrl != null) {
             recordClick(shortCode);
             return cachedLongUrl;
         }
        //缓存没有，查询数据库
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrl == null) {
            //数据库没有，返回null
            return null;
        }
        //更新点击次数
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrlRepository.save(shortUrl);
        recordClick(shortCode);


        //更新缓存
        redisTemplate.opsForValue().set(
                "short_url:" + shortCode,
                shortUrl.getLongUrl(),
                Duration.ofHours(24) // 设置 24 小时过期
        );

        //返回长链接
        return shortUrl.getLongUrl();
    }


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
       dailyClick.setShortCode(customAlias);
       dailyClickRepo.save(dailyClick);

        
        // 更新缓存
        redisTemplate.delete("short_url:" + shortCode);
        redisTemplate.opsForValue().set(
                "short_url:" + customAlias,
                shortUrl.getLongUrl(),
                Duration.ofHours(24)
        );

    }
}