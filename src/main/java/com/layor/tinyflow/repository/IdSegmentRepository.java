package com.layor.tinyflow.repository;

import com.layor.tinyflow.entity.IdSegment;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface IdSegmentRepository extends JpaRepository<IdSegment, String> {


    /**

     * 原子更新 max_id（关键！）

     * 使用数据库行锁保证并发安全

     */

    @Modifying

    @Transactional

    @Query(value = "UPDATE id_segment SET max_id = max_id + :step WHERE biz_tag = :bizTag", nativeQuery = true)

    int updateMaxId(@Param("bizTag") String bizTag, @Param("step") int step);


    /**

     * 查询当前 max_id

     */

    @Query("SELECT s.maxId FROM IdSegment s WHERE s.bizTag = :bizTag")

    Long findMaxIdByBizTag(@Param("bizTag") String bizTag);

}
