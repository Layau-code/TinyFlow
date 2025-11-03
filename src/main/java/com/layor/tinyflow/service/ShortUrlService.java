package com.layor.tinyflow.service;

import com.layor.tinyflow.entity.ShortUrl;
import com.layor.tinyflow.entity.ShortUrlDTO;
import com.layor.tinyflow.repository.ShortUrlRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ShortUrlService {
    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    // 短码长度
    private static final int CODE_LENGTH = 6;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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

        //更新点击次数
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrlRepository.save(shortUrl);

        //更新缓存
        redisTemplate.opsForValue().set(
                "short_url:" + shortCode,
                shortUrl.getLongUrl(),
                Duration.ofHours(24) // 设置 24 小时过期
        );

        //返回长链接
        return shortUrl.getLongUrl();
    }
}