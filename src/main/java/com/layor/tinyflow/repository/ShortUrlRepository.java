package com.layor.tinyflow.repository;


import com.layor.tinyflow.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    ShortUrl findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    ShortUrl findByLongUrl(String longUrl);
    List<ShortUrl> findAllByOrderByCreatedAtDesc();
}