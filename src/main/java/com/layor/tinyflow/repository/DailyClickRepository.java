package com.layor.tinyflow.repository;

import com.layor.tinyflow.entity.DailyClick;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyClickRepository extends JpaRepository<DailyClick, Long> {

    @Query("SELECT d FROM DailyClick d WHERE d.shortCode = :shortCode AND d.date = :date")
    Optional<DailyClick> findByShortCodeAndDate(@Param("shortCode") String shortCode,
                                                @Param("date") LocalDate date);


    @Query("SELECT d FROM DailyClick d WHERE d.shortCode = :shortCode " +
            "AND d.date >= :startDate ORDER BY d.date")
    List<DailyClick> findRecentTrend(@Param("shortCode") String shortCode,
                                     @Param("startDate") LocalDate startDate);
    @Query("SELECT d.clicks FROM DailyClick d WHERE d.shortCode = :shortCode AND d.date = CURRENT_DATE")
    Integer getTodayClicksByShortCode(String shortCode);

    DailyClick findByShortCode(String shortCode);

    void deleteByShortCode(String shortCode);
    @Modifying
    @Query(value = """
    INSERT INTO daily_click (short_code, date, clicks)
    VALUES (:shortCode, CURDATE(), 1)
    ON DUPLICATE KEY UPDATE clicks = clicks + 1
    """, nativeQuery = true)
    void incrementClick(@Param("shortCode") String shortCode);
}