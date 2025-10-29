package com.layor.tinyflow.service;

import com.layor.tinyflow.entity.ShortUrl;
import com.layor.tinyflow.repository.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ShortUrlService {

    @Autowired
    private ShortUrlRepository repository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    public String generateShortCode(String longUrl) {
        // 先查数据库和缓存，避免重复
        String cached = redisTemplate.opsForValue().get("short:" + longUrl);
        if (cached != null) {
            return cached;
        }

        String shortCode;
        do {
            shortCode = generateRandomCode();
        } while (repository.findByShortCode(shortCode) != null);

        ShortUrl shortUrl = new ShortUrl(longUrl, shortCode);
        repository.save(shortUrl);

        // 缓存到 Redis
        redisTemplate.opsForValue().set("short:" + shortCode, longUrl);
        redisTemplate.opsForValue().set("short:" + longUrl, shortCode);

        return shortCode;
    }

    public String getLongUrl(String shortCode) {
        // 先查 Redis
        String longUrl = redisTemplate.opsForValue().get("short:" + shortCode);
        if (longUrl != null) {
            // 增加点击次数（可选）
            redisTemplate.opsForValue().increment("click:" + shortCode);
            return longUrl;
        }

        // 查数据库
        ShortUrl shortUrl = repository.findByShortCode(shortCode);
        if (shortUrl != null) {
            // 回写缓存
            redisTemplate.opsForValue().set("short:" + shortCode, shortUrl.getLongUrl());
            return shortUrl.getLongUrl();
        }
        return null;
    }

    private String generateRandomCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}