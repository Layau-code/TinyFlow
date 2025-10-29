package com.layor.tinyflow.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "short_url")
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "long_url", nullable = false)
    private String longUrl;

    @Column(name = "short_code", unique = true, nullable = false)
    private String shortCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "click_count")
    private Integer clickCount = 0;

    // 无参构造
    public ShortUrl() {
        this.createdAt = LocalDateTime.now();
    }

    // 全参构造
    public ShortUrl(String longUrl, String shortCode) {
        this();
        this.longUrl = longUrl;
        this.shortCode = shortCode;
    }

    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLongUrl() { return longUrl; }
    public void setLongUrl(String longUrl) { this.longUrl = longUrl; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getClickCount() { return clickCount; }
    public void setClickCount(Integer clickCount) { this.clickCount = clickCount; }
}
