package com.layor.tinyflow.service;

import com.layor.tinyflow.entity.IdSegment;
import com.layor.tinyflow.repository.IdSegmentRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

@Service
public class SegmentIdGenerator implements IdGenerator {

    private  final IdSegmentRepository segmentRepo;



    private final Map<String, Buffer> bufferCache = new ConcurrentHashMap<>();

    public SegmentIdGenerator(IdSegmentRepository segmentRepo) {
        this.segmentRepo = segmentRepo;
    }

    @Override
    public long nextId(String bizTag) {
        // 每个 bizTag 对应一个 Buffer（懒加载）
        return bufferCache.computeIfAbsent(bizTag, Buffer::new).nextId();
    }

    /**
     * 每个业务标签（bizTag）对应一个独立的 ID 缓冲区
     */
    class Buffer {
        private final String bizTag;
        private static final int STEP = 100_000; // 每次预取 10w 个 ID

        // 双缓冲：current 正在使用，next 预加载（避免切换时阻塞）
        private volatile Range currentRange;
        private volatile Range nextRange;

        Buffer(String bizTag) {
            this.bizTag = bizTag;
            initialize(); // 初始化两个号段
        }

        /**
         * 获取下一个可用 ID
         */
        synchronized long nextId() {
            long id = currentRange.next();
            if (id != -1) {
                return id; // 当前号段还有余量
            }

            // 当前号段耗尽，切换到预加载的下一段
            currentRange = nextRange;
            nextRange = allocateNewRange(); // 同步拉取新段（可后续优化为异步）
            return currentRange.next();
        }

        /**
         * 初始化：确保 DB 有记录，并加载前两段
         */
        private void initialize() {
            ensureSegmentExistsInDb();
            this.currentRange = allocateNewRange();
            this.nextRange = allocateNewRange();
        }

        /**
         * 确保数据库中存在该 bizTag 的记录（首次使用时创建）
         */
        private void ensureSegmentExistsInDb() {
            // 先检查是否存在
            if (!segmentRepo.existsById(bizTag)) {
                try {
                    IdSegment segment = new IdSegment();
                    segment.setBizTag(bizTag);
                    segment.setMaxId(1L);
                    segment.setStep(STEP);
                    segmentRepo.save(segment);
                } catch (Exception ex) {
                    // 并发场景下可能多个实例同时插入，由 DB 唯一约束保证最终只有一个成功
                    // 这里忽略异常，后续 fetch 仍能成功
                }
            }
        }

        /**
         * 从数据库原子地申请一个新的 ID 号段 [start, end]
         */
        private Range allocateNewRange() {
            // 1. 原子更新 max_id（关键：DB 行锁保证并发安全）
            int updatedRows = segmentRepo.updateMaxId(bizTag, STEP);
            if (updatedRows == 0) {
                throw new RuntimeException("Failed to update max_id for bizTag: " + bizTag);
            }

            // 2. 查询更新后的 max_id（即新区间的结束值）
            Long newMaxId = segmentRepo.findMaxIdByBizTag(bizTag);
            if (newMaxId == null) {
                throw new IllegalStateException("Segment record missing after update: " + bizTag);
            }

            long end = newMaxId;
            long start = end - STEP + 1;
            return new Range(start, end);
        }
    }

    /**
     * 表示一个连续的 ID 区间 [start, end]
     */
    static class Range {
        private final long end;
        private final AtomicLong cursor; // 当前已分配到的位置

        Range(long start, long end) {
            this.end = end;
            this.cursor = new AtomicLong(start - 1); // 下一次 incrementAndGet 得到 start
        }

        /**
         * 返回下一个 ID，若耗尽返回 -1
         */
        long next() {
            long value = cursor.incrementAndGet();
            return (value <= end) ? value : -1;
        }
    }
}