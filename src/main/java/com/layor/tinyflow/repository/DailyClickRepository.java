package com.layor.tinyflow.repository;

import com.layor.tinyflow.entity.DailyClick;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT d.shortCode, d.clicks FROM DailyClick d WHERE d.date = CURRENT_DATE AND d.shortCode IN (:codes)")
    List<Object[]> findTodayClicksByShortCodes(@Param("codes") List<String> codes);

    DailyClick findByShortCode(String shortCode);

    void deleteByShortCode(String shortCode);
    @Modifying
    @Query(value = """
    INSERT INTO daily_click (short_code, date, clicks)
    VALUES (:shortCode, CURDATE(), 1)
    ON DUPLICATE KEY UPDATE clicks = clicks + 1
    """, nativeQuery = true)
    void incrementClick(@Param("shortCode") String shortCode);

    @Modifying
    @Query(value = """
    INSERT INTO daily_click (short_code, date, clicks)
    VALUES (:shortCode, CURDATE(), :delta)
    ON DUPLICATE KEY UPDATE clicks = clicks + :delta
    """, nativeQuery = true)
    void incrementClickBy(@Param("shortCode") String shortCode, @Param("delta") long delta);

    @Query("SELECT d.shortCode FROM DailyClick d WHERE d.date = CURRENT_DATE AND d.clicks > 0")
    List<String> findTodayActiveCodes();
}