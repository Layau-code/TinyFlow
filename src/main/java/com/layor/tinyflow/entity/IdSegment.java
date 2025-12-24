package com.layor.tinyflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "id_segment")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdSegment {

    @Id
    @Column(name = "biz_tag", length = 64)
    private String bizTag;

    @Column(name = "max_id", nullable = false)
    @Builder.Default
    private long maxId = 1L;

    @Column(name = "step", nullable = false)
    @Builder.Default
    private int step = 100_000;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private int version = 0;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}