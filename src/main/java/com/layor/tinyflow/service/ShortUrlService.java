package com.layor.tinyflow.service;

import com.layor.tinyflow.entity.ShortUrl;
import com.layor.tinyflow.repository.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
        // 1. 先查缓存：长链接 → 短码
        String cachedShortCode = redisTemplate.opsForValue().get("short:" + longUrl);
        if (cachedShortCode != null) {
            return cachedShortCode;
        }

        // 2. 查数据库：是否已有该长链接的记录？
        ShortUrl existing = repository.findByLongUrl(longUrl);
        if (existing != null) {
            // 2.1 如果已有，更新缓存并返回
            String shortCode = existing.getShortCode();
            redisTemplate.opsForValue().set("short:" + longUrl, shortCode, Duration.ofHours(24));
            redisTemplate.opsForValue().set("short:code:" + shortCode, longUrl, Duration.ofHours(24));
            return shortCode;
        }

        // 3. 没有记录，生成新短码
        String shortCode;
        int attempts = 0;
        do {
            shortCode = generateRandomCode(); // 如 Base62 编码 or 随机字符串
            attempts++;
            if (attempts > 5) {
                throw new RuntimeException("Failed to generate unique short code");
            }
        } while (repository.existsByShortCode(shortCode)); // 检查短码是否被占用

        // 4. 保存到数据库（关键：这里要保证原子性）
        ShortUrl newShortUrl = new ShortUrl(longUrl, shortCode);
        ShortUrl saved = repository.save(newShortUrl);

        // 5. 写入 Redis 缓存（双向）
        redisTemplate.opsForValue().set("short:" + longUrl, shortCode, Duration.ofHours(24));
        redisTemplate.opsForValue().set("short:code:" + shortCode, longUrl, Duration.ofHours(24));

        return saved.getShortCode();
    }

    public String getLongUrl(String shortCode) {
        // 先查 Redis
        String longUrl = redisTemplate.opsForValue().get("short:" + shortCode);
        if (longUrl != null) {
            // 增加点击次数
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