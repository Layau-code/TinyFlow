package com.layor.tinyflow.repository;


import com.layor.tinyflow.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    ShortUrl findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    void deleteByShortCode(String shortCode);

    boolean existsByLongUrl(String longUrl);

    ShortUrl findByLongUrl(String longUrl);
}