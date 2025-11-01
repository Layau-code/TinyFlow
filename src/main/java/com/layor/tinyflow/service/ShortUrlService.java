package com.layor.tinyflow.service;

import com.layor.tinyflow.entity.ShortUrlDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ShortUrlService {

    // 模拟数据库存储（实际项目替换为 JPA/MyBatis）
    private final Map<String, String> shortCodeToLongUrl = new ConcurrentHashMap<>();
    private final Map<String, ShortUrlDTO> shortCodeToDto = new ConcurrentHashMap<>();

    // 用于生成随机码的计数器（演示用，实际用更安全的算法）
    private final AtomicLong counter = new AtomicLong(0);

    // 基础短链域名
    private static final String BASE_URL = "https://go.to";

    @PostConstruct
    public void init() {
        // 可以预加载一些数据
    }

    /**
     * 生成短链
     */
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
            // 清理非法字符
            shortCode = sanitizeAlias(shortCode.trim());
            if (shortCode.isEmpty()) {
                throw new Exception("别名不能只包含非法字符");
            }
            // 检查是否已存在
            if (shortCodeToLongUrl.containsKey(shortCode)) {
                throw new Exception("该别名已被占用，请换一个");
            }
        }

        // 3. 保存到“数据库”
        String shortUrl = BASE_URL + "/" + shortCode;
        shortCodeToLongUrl.put(shortCode, longUrl);
        ShortUrlDTO dto = new ShortUrlDTO(shortCode, shortUrl, longUrl);
        shortCodeToDto.put(shortCode, dto);

        return dto;
    }

    /**
     * 根据短码获取原始链接
     */
    public String getLongUrlByShortCode(String shortCode) {
        return shortCodeToLongUrl.get(shortCode);
    }

    /**
     * 校验是否为合法 URL
     */
    private boolean isValidUrl(String url) {
        try {
            new URI(url).parseServerAuthority();
            return url != null && (url.startsWith("http://") || url.startsWith("https://"));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * 清理别名中的非法字符
     */
    private String sanitizeAlias(String alias) {
        return alias.replaceAll("[/?#&=%<>\"'\\\\]", ""); // 移除危险字符
    }

    /**
     * 生成6位随机码（演示用）
     * 实际项目建议使用 Base62 或雪花算法
     */
    private String generateRandomCode() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        long num = counter.incrementAndGet();
        while (num > 0) {
            sb.append(chars.charAt((int) (num % 62)));
            num /= 62;
        }
        // 补足6位
        while (sb.length() < 6) {
            sb.append('a');
        }
        return sb.toString();
    }
}