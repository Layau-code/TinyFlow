package com.layor.tinyflow.service;

import com.layor.tinyflow.entity.ShortUrl;
import com.layor.tinyflow.entity.ShortUrlMessage;
import com.layor.tinyflow.repository.ShortUrlRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 短链持久化消费者
 * 
 * 工作流程：
 * 1. 消费MQ消息
 * 2. 持久化到MySQL
 * 3. 失败重试（3次）
 * 4. 超过重试次数进入死信队列
 */
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.host")
@Slf4j
public class ShortUrlPersistenceConsumer {
    
    @Autowired
    private ShortUrlRepository shortUrlRepository;
    
    // 监控指标
    private final AtomicLong totalProcessed = new AtomicLong(0);
    private final AtomicLong totalSuccess = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
    
    /**
     * 消费短链持久化消息
     */
    @RabbitListener(queues = "tinyflow.shorturl.queue")
    @Transactional
    public void consumePersistenceMessage(ShortUrlMessage message) {
        if (message == null || message.getShortCode() == null) {
            log.warn("[MQ] Received invalid message: {}", message);
            return;
        }
        
        try {
            totalProcessed.incrementAndGet();
            
            // 检查是否已存在（幂等性保证）
            if (shortUrlRepository.existsByShortCode(message.getShortCode())) {
                log.debug("[SKIP] 短链 {} 已存在，跳过持久化", message.getShortCode());
                totalSuccess.incrementAndGet();
                return;
            }
            
            // 持久化到数据库
            ShortUrl shortUrl = ShortUrl.builder()
                    .shortCode(message.getShortCode())
                    .longUrl(message.getLongUrl())
                    .userId(message.getUserId())
                    .createdAt(message.getCreatedAt())
                    .clickCount(0)
                    .build();
            
            shortUrlRepository.save(shortUrl);
            totalSuccess.incrementAndGet();
            
            log.info("[SUCCESS] 短链 {} 已持久化到数据库, total={}, success={}", 
                    message.getShortCode(), totalProcessed.get(), totalSuccess.get());
            
        } catch (Exception e) {
            totalFailed.incrementAndGet();
            log.error("[FAILED] 短链 {} 持久化失败: {}, retry={}, total={}, failed={}", 
                    message.getShortCode(), e.getMessage(), 
                    message.getRetryCount(), totalProcessed.get(), totalFailed.get());
            
            // 抛出异常触发重试机制（RabbitMQ会自动重试）
            throw new RuntimeException("Persistence failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取监控指标
     */
    public String getMetrics() {
        return String.format(
            "ShortUrlPersistence Metrics:\n" +
            "  Total Processed: %d\n" +
            "  Success: %d\n" +
            "  Failed: %d\n" +
            "  Success Rate: %.2f%%\n",
            totalProcessed.get(),
            totalSuccess.get(),
            totalFailed.get(),
            totalProcessed.get() > 0 ? (totalSuccess.get() * 100.0 / totalProcessed.get()) : 0
        );
    }
}
