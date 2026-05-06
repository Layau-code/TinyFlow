package com.layor.tinyflow.service;

import com.layor.tinyflow.config.RabbitMQConfig;
import com.layor.tinyflow.entity.ClickMessage;
import com.layor.tinyflow.repository.DailyClickRepository;
import com.layor.tinyflow.repository.ShortUrlRepository;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RabbitMQ 消息消费者
 * 批量消费点击事件消息，批量更新数据库
 * 
 * 只有在配置了 RabbitMQ 时才启用
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class ClickMessageConsumer {
    
    @Autowired
    private ShortUrlRepository shortUrlRepository;
    
    @Autowired
    private DailyClickRepository dailyClickRepo;
    
    // 本地批量累加缓存
    private final Map<String, AtomicLong> totalClicksBuffer = new ConcurrentHashMap<>();
    private final Map<String, Map<String, AtomicLong>> dailyClicksBuffer = new ConcurrentHashMap<>();
    
    /**
     * 消费点击消息（批量预取 prefetch=100）
     * 使用手动确认模式，保证消息不丢失
     */
    @RabbitListener(queues = RabbitMQConfig.CLICK_QUEUE)
    public void consumeClickMessage(ClickMessage message, Channel channel, Message amqpMessage) throws IOException {
        try {
            if (message == null || message.getShortCode() == null) {
                log.warn("[MQ CONSUMER] Invalid message received");
                // 拒绝消息，不重新入队
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
                return;
            }
            
            String shortCode = message.getShortCode();
            String date = message.getDate();
            
            // 累加到本地缓冲区
            totalClicksBuffer.computeIfAbsent(shortCode, k -> new AtomicLong()).incrementAndGet();
            dailyClicksBuffer.computeIfAbsent(date, k -> new ConcurrentHashMap<>())
                             .computeIfAbsent(shortCode, k -> new AtomicLong())
                             .incrementAndGet();
            
            log.debug("[MQ CONSUMER] Buffered click: shortCode={}, date={}", shortCode, date);
            
            // 每累积100条消息批量刷库一次（或定时刷库）
            if (totalClicksBuffer.values().stream().mapToLong(AtomicLong::get).sum() >= 100) {
                flushToDatabase();
            }
            
            // 手动确认消息
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
            
        } catch (Exception e) {
            log.error("[MQ CONSUMER ERROR] Failed to process message: {}", e.getMessage(), e);
            // 拒绝消息并重新入队（最多重试3次）
            int retryCount = getRetryCount(amqpMessage);
            if (retryCount < 3) {
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, true);
            } else {
                log.error("[MQ CONSUMER] Message exceeded max retries, sending to DLQ: {}", message);
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
            }
        }
    }
    
    /**
     * 定时批量刷库（每2秒一次）
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 2000)
    @Transactional
    public void flushToDatabase() {
        if (totalClicksBuffer.isEmpty() && dailyClicksBuffer.isEmpty()) {
            return;
        }
        
        long start = System.currentTimeMillis();
        int totalCount = 0;
        int dailyCount = 0;
        
        try {
            // 批量更新总点击数
            for (Map.Entry<String, AtomicLong> entry : totalClicksBuffer.entrySet()) {
                String code = entry.getKey();
                long delta = entry.getValue().getAndSet(0);
                if (delta > 0) {
                    shortUrlRepository.incrementClickCountBy(code, delta);
                    totalCount++;
                }
            }
            
            // 批量更新每日点击数
            for (Map.Entry<String, Map<String, AtomicLong>> dateEntry : dailyClicksBuffer.entrySet()) {
                for (Map.Entry<String, AtomicLong> codeEntry : dateEntry.getValue().entrySet()) {
                    String code = codeEntry.getKey();
                    long delta = codeEntry.getValue().getAndSet(0);
                    if (delta > 0) {
                        dailyClickRepo.incrementClickBy(code, delta);
                        dailyCount++;
                    }
                }
            }
            
            log.info("[MQ FLUSH] Flushed {} total clicks and {} daily clicks to DB, duration={}ms", 
                totalCount, dailyCount, System.currentTimeMillis() - start);
            
        } catch (Exception e) {
            log.error("[MQ FLUSH ERROR] Failed to flush to database: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 获取消息重试次数
     */
    private int getRetryCount(Message message) {
        List<Map<String, ?>> xDeathHeader = (List<Map<String, ?>>) message.getMessageProperties()
            .getHeaders().get("x-death");
        if (xDeathHeader != null && !xDeathHeader.isEmpty()) {
            Object count = xDeathHeader.get(0).get("count");
            if (count instanceof Long) {
                return ((Long) count).intValue();
            }
        }
        return 0;
    }
}
