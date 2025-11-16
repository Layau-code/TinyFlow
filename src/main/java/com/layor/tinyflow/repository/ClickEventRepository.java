package com.layor.tinyflow.repository;

import com.layor.tinyflow.entity.ClickEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    @Query("select e.sourceHost, count(e) from ClickEvent e where e.shortCode=:code and e.ts between :start and :end and (:source is null or :source='' or e.sourceHost=:source) group by e.sourceHost order by count(e) desc")
    List<Object[]> countBySource(@Param("code") String code,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 @Param("source") String source);

    @Query("select e.deviceType, count(e) from ClickEvent e where e.shortCode=:code and e.ts between :start and :end and (:device is null or :device='' or e.deviceType=:device) group by e.deviceType order by count(e) desc")
    List<Object[]> countByDevice(@Param("code") String code,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 @Param("device") String device);

    @Query("select e.city, count(e) from ClickEvent e where e.shortCode=:code and e.ts between :start and :end and (:city is null or :city='' or e.city=:city) group by e.city order by count(e) desc")
    List<Object[]> countByCity(@Param("code") String code,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end,
                               @Param("city") String city);

    @Query("select e from ClickEvent e where e.shortCode=:code and e.ts between :start and :end and (:source is null or :source='' or e.sourceHost=:source) and (:device is null or :device='' or e.deviceType=:device) and (:city is null or :city='' or e.city=:city)")
    Page<ClickEvent> findEvents(@Param("code") String code,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("source") String source,
                                @Param("device") String device,
                                @Param("city") String city,
                                Pageable pageable);
}