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

    // 按小时统计访问量
    @Query("select hour(e.ts), count(e) from ClickEvent e where e.shortCode=:code and e.ts between :start and :end group by hour(e.ts) order by hour(e.ts)")
    List<Object[]> countByHour(@Param("code") String code,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    // 按星期统计访问量
    @Query("select dayofweek(e.ts), count(e) from ClickEvent e where e.shortCode=:code and e.ts between :start and :end group by dayofweek(e.ts) order by dayofweek(e.ts)")
    List<Object[]> countByDayOfWeek(@Param("code") String code,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    // 按国家统计
    @Query("select e.country, count(e) from ClickEvent e where e.shortCode=:code and e.ts between :start and :end group by e.country order by count(e) desc")
    List<Object[]> countByCountry(@Param("code") String code,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    // 按UA/浏览器统计
    @Query("select e.ua, count(e) from ClickEvent e where e.shortCode=:code and e.ts between :start and :end group by e.ua order by count(e) desc")
    List<Object[]> countByUa(@Param("code") String code,
                             @Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end);

    // 按Referer详细统计
    @Query("select e.referer, count(e) from ClickEvent e where e.shortCode=:code and e.ts between :start and :end and e.referer is not null group by e.referer order by count(e) desc")
    List<Object[]> countByReferer(@Param("code") String code,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    // 统计总访问量
    @Query("select count(e) from ClickEvent e where e.shortCode=:code and e.ts between :start and :end")
    long countTotal(@Param("code") String code,
                    @Param("start") LocalDateTime start,
                    @Param("end") LocalDateTime end);

    // UV统计(独立IP)
    @Query("select count(distinct e.ip) from ClickEvent e where e.shortCode=:code and e.ts between :start and :end")
    long countUniqueIp(@Param("code") String code,
                       @Param("start") LocalDateTime start,
                       @Param("end") LocalDateTime end);

    // 全局统计：按日期统计所有短链的访问量
    @Query("select date(e.ts), count(e) from ClickEvent e where e.ts between :start and :end group by date(e.ts) order by date(e.ts)")
    List<Object[]> countAllByDate(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    // 全局统计：按设备统计
    @Query("select e.deviceType, count(e) from ClickEvent e where e.ts between :start and :end group by e.deviceType order by count(e) desc")
    List<Object[]> countAllByDevice(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    // 全局统计：按城市统计
    @Query("select e.city, count(e) from ClickEvent e where e.ts between :start and :end group by e.city order by count(e) desc")
    List<Object[]> countAllByCity(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    // 全局统计：按来源统计
    @Query("select e.sourceHost, count(e) from ClickEvent e where e.ts between :start and :end group by e.sourceHost order by count(e) desc")
    List<Object[]> countAllBySource(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    // 全局统计：总量PV
    @Query("select count(e) from ClickEvent e where e.ts between :start and :end")
    long countAllTotal(@Param("start") LocalDateTime start,
                       @Param("end") LocalDateTime end);

    // 全局统计：总量UV
    @Query("select count(distinct e.ip) from ClickEvent e where e.ts between :start and :end")
    long countAllUniqueIp(@Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);

    // 统计某短链的最早访问时间
    @Query("select min(e.ts) from ClickEvent e where e.shortCode=:code")
    LocalDateTime findFirstClickTime(@Param("code") String code);

    // 统计某短链的最后访问时间
    @Query("select max(e.ts) from ClickEvent e where e.shortCode=:code")
    LocalDateTime findLastClickTime(@Param("code") String code);
}