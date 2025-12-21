package com.layor.tinyflow.repository;


import com.layor.tinyflow.entity.ShortUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    ShortUrl findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    void deleteByShortCode(String shortCode);

    boolean existsByLongUrl(String longUrl);

    // 根据 longUrl 查询（可能多个结果，已废弃）
    @Deprecated
    ShortUrl findByLongUrl(String longUrl);
    
    /**
     * 根据用户ID和长链接查询短链（唯一）
     */
    ShortUrl findByUserIdAndLongUrl(Long userId, String longUrl);
    
    /**
     * 查询匿名短链（userId 为 null）
     */
    ShortUrl findByUserIdIsNullAndLongUrl(String longUrl);

    @Transactional
    @Modifying(clearAutomatically = false, flushAutomatically = false)
    @Query("update ShortUrl s set s.clickCount = s.clickCount + 1 where s.shortCode = :shortCode")
    void incrementClickCount(@Param("shortCode") String shortCode);


    @Transactional
    @Modifying(clearAutomatically = false, flushAutomatically = false)
    @Query("update ShortUrl s set s.clickCount = s.clickCount + :delta where s.shortCode = :shortCode")
    void incrementClickCountBy(@Param("shortCode") String shortCode, @Param("delta") long delta);
    
    /**
     * 根据用户ID分页查询短链接
     */
    Page<ShortUrl> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 查询所有匿名短链（userId 为 null）
     */
    Page<ShortUrl> findByUserIdIsNull(Pageable pageable);
    
    /**
     * 根据用户ID和短码查询
     */
    ShortUrl findByUserIdAndShortCode(Long userId, String shortCode);
    
    /**
     * 根据用户ID查询所有短链接
     */
    List<ShortUrl> findByUserId(Long userId);
}