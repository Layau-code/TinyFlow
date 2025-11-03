package com.layor.tinyflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_click", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"short_code", "date"}) // 防止重复插入
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyClick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", nullable = false)
    private String shortCode;

    @Column(name = "date", nullable = false)
    private LocalDate date;
    @Column(name = "clicks", nullable = false)
    private Integer clicks = 0;
}