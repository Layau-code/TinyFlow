package com.layor.tinyflow.repository;


import com.layor.tinyflow.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    ShortUrl findByShortCode(String shortCode);
}