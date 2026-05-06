package com.layor.tinyflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder
@Table(name = "short_url", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_short_code", columnList = "short_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "long_url", nullable = false)
    private String longUrl;

    @Column(name = "short_code", unique = true, nullable = false)
    private String shortCode;
    
    /**
     * 关联用户ID（创建者）
     */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "click_count")
    @Builder.Default
    private Integer clickCount = 0;
}
