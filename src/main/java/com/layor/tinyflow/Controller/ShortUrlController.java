package com.layor.tinyflow.Controller;

import com.layor.tinyflow.service.ShortUrlService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ShortUrlController {

    @Autowired
    private ShortUrlService shortUrlService;

    @Value("${app.domain}")
    private String domain;

    // 生成短链
    @PostMapping("/shorten")

    public ResponseEntity<String> shorten(@RequestBody String longUrl) {
        if (longUrl == null || longUrl.isBlank()) {
            return ResponseEntity.badRequest().body("URL 不能为空");
        }
        String shortCode = shortUrlService.generateShortCode(longUrl);
        String shortUrl = domain + "/" + shortCode;
        return ResponseEntity.ok(shortUrl);
    }

    // 跳转
    @GetMapping("/{shortCode}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode) {
        String longUrl = shortUrlService.getLongUrl(shortCode);
        if (longUrl != null) {
            return ResponseEntity.status(302)
                    .header("Location", longUrl)
                    .build();
        }
        return ResponseEntity.notFound().build();
    }
}