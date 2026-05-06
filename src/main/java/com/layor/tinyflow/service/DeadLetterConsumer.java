package com.layor.tinyflow.service;

import com.layor.tinyflow.config.RabbitMQConfig;
import com.layor.tinyflow.entity.ClickMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 死信队列消费者
 * 处理消费失败的消息，记录日志并告警
 * 
 * 只有在配置了 RabbitMQ 时才启用
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class DeadLetterConsumer {
    
    /**
     * 消费死信队列中的消息
     * 这些是经过多次重试仍然失败的消息
     */
    @RabbitListener(queues = RabbitMQConfig.DLX_QUEUE)
    public void consumeDeadLetter(ClickMessage message, Channel channel, Message amqpMessage) throws IOException {
        try {
            log.error("[DLQ] Dead letter received: shortCode={}, timestamp={}, date={}", 
                message.getShortCode(), 
                message.getTimestamp(), 
                message.getDate());
            
            // TODO: 这里可以做告警处理
            // 1. 发送钉钉/企业微信告警
            // 2. 记录到专门的失败日志表
            // 3. 发送邮件通知运维
            // 4. 上报到监控系统（如 Prometheus）
            
            // 示例：记录详细信息用于排查
            log.error("[DLQ] Message details - Headers: {}, Body: {}", 
                amqpMessage.getMessageProperties().getHeaders(), 
                new String(amqpMessage.getBody()));
            
            // 确认消息（从死信队列中移除）
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
            
        } catch (Exception e) {
            log.error("[DLQ ERROR] Failed to process dead letter: {}", e.getMessage(), e);
            // 拒绝消息，不重新入队（避免死信队列也堆积）
            channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
        }
    }
}
