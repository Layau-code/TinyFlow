package com.layor.tinyflow.repository;


import com.layor.tinyflow.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    ShortUrl findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    void deleteByShortCode(String shortCode);

    boolean existsByLongUrl(String longUrl);

    ShortUrl findByLongUrl(String longUrl);

    @Transactional
    @Modifying(clearAutomatically = false, flushAutomatically = false)
    @Query("update ShortUrl s set s.clickCount = s.clickCount + 1 where s.shortCode = :shortCode")
    void incrementClickCount(@Param("shortCode") String shortCode);


    @Transactional
    @Modifying(clearAutomatically = false, flushAutomatically = false)
    @Query("update ShortUrl s set s.clickCount = s.clickCount + :delta where s.shortCode = :shortCode")
    void incrementClickCountBy(@Param("shortCode") String shortCode, @Param("delta") long delta);
}