package com.layor.tinyflow.Controller;

import com.layor.tinyflow.entity.Result;
import com.layor.tinyflow.entity.ShortUrlDTO;
import com.layor.tinyflow.entity.ShortenRequest;
import com.layor.tinyflow.service.ShortUrlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ShortUrlController {

    @Autowired
    private ShortUrlService shortUrlService;

    /**
     * 生成短链（支持自定义别名）
     */
    @PostMapping("/shorten")
    public Result<ShortUrlDTO> shorten(@Valid @RequestBody ShortenRequest request) throws Exception {
            ShortUrlDTO dto = shortUrlService.createShortUrl(request.getLongUrl(), request.getCustomAlias());
            return Result.success(dto);
    }

    /**
     * 短链跳转（实际使用时用 Nginx 或 WebFlux 实现更高效）
     */
    @GetMapping("/{shortCode}")
    public Result<String> redirect(@PathVariable String shortCode) {
        String longUrl = shortUrlService.getLongUrlByShortCode(shortCode);
        if (longUrl == null) {
            return Result.error(1004, "短链不存在");
        }
        // 实际应返回 302 重定向，这里演示返回链接
        return Result.success("Redirect to: " + longUrl);
    }
}