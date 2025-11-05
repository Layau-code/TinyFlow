package com.layor.tinyflow.Controller;

import com.layor.tinyflow.entity.*;
import com.layor.tinyflow.service.ShortUrlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
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
        return Result.success("Redirect to: " + longUrl);
    }


    @GetMapping("/urls")
    public ResponseEntity<PageResponseDTO<UrlListResponseDTO>> getUrls(
            Pageable pageable) {  // ← 让 Spring 自动解析 page & size

        Page<UrlListResponseDTO> urls = shortUrlService.getAllUrls(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );


        PageResponseDTO<UrlListResponseDTO> response = new PageResponseDTO<>(
                urls.getContent(),
                urls.getTotalElements(),
                urls.getTotalPages(),
                urls.getSize(),
                urls.getNumber(),
                urls.isFirst(),
                urls.isLast(),
                urls.isEmpty()
        );

        return ResponseEntity.ok(response);
    }
}