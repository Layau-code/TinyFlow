package com.layor.tinyflow.Controller;

import com.layor.tinyflow.entity.*;
import com.layor.tinyflow.service.ShortUrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ShortUrlController {

    @Autowired
    private ShortUrlService shortUrlService;

    /**
     * 生成短链（支持自定义别名）
     */
    @PostMapping("/api/shorten")
    public Result<ShortUrlDTO> shorten(@Valid @RequestBody ShortenRequest request) throws Exception {
            ShortUrlDTO dto = shortUrlService.createShortUrl(request.getLongUrl(), request.getCustomAlias());
            return Result.success(dto);
    }
    // 根路径重定向（外部短链）
    @GetMapping("/{shortCode}")
    public void redirectRoot(@PathVariable String shortCode, HttpServletRequest request, HttpServletResponse response) {
        shortUrlService.redirectCode(shortCode, request, response);
    }
    //修改短链
    @PutMapping("/api/{shortCode}")
    public void updateShortUrl(@Valid @RequestBody ShortenRequest request) {
        shortUrlService.updateShortUrl( request.getShortCode(),request.getCustomAlias());
    }
    @DeleteMapping("/api/{shortCode}")
    public void deleteHistory(@PathVariable String shortCode) {
        shortUrlService.deleteByShortCode(shortCode);

    }


    @GetMapping("/api/urls")
    public Result<PageResponseDTO<UrlListResponseDTO>> getUrls(
            Pageable pageable) {

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

        return Result.success(response);
    }
    @GetMapping("/api/urls/click-stats")
    public Result<List<UrlClickStatsDTO>> getUrlClickStats() {
        List<UrlClickStatsDTO> stats = shortUrlService.getUrlClickStats();
        return Result.success(stats);
    }

    //内部使用，内部跳转，压测监控
    @GetMapping ("/api/redirect/{shortCode}")
    public void redirect(@PathVariable String shortCode, HttpServletRequest request, HttpServletResponse response) {
        shortUrlService.redirectCode(shortCode, request, response);
    }

}
