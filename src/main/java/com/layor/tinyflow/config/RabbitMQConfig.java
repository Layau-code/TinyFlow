package com.layor.tinyflow.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 配置点击事件队列、死信队列、消息转换器等
 */
@Configuration
public class RabbitMQConfig {
    
    // ========== 队列名称常量 ==========
    public static final String CLICK_QUEUE = "tinyflow.click.queue";
    public static final String CLICK_EXCHANGE = "tinyflow.click.exchange";
    public static final String CLICK_ROUTING_KEY = "click";
    
    // ========== 死信队列 ==========
    public static final String DLX_QUEUE = "tinyflow.click.dlq";
    public static final String DLX_EXCHANGE = "tinyflow.click.dlx";
    public static final String DLX_ROUTING_KEY = "click.dead";
    
    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }
    
    /**
     * 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLX_QUEUE).build();
    }
    
    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DLX_ROUTING_KEY);
    }
    
    /**
     * 点击事件交换机
     */
    @Bean
    public DirectExchange clickExchange() {
        return new DirectExchange(CLICK_EXCHANGE, true, false);
    }
    
    /**
     * 点击事件队列（配置死信队列）
     */
    @Bean
    public Queue clickQueue() {
        return QueueBuilder.durable(CLICK_QUEUE)
                // 配置死信交换机
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLX_ROUTING_KEY)
                // 消息TTL：30秒未消费则进入死信队列
                .withArgument("x-message-ttl", 30000)
                .build();
    }
    
    /**
     * 绑定点击队列到交换机
     */
    @Bean
    public Binding clickBinding() {
        return BindingBuilder.bind(clickQueue())
                .to(clickExchange())
                .with(CLICK_ROUTING_KEY);
    }
    
    /**
     * 消息转换器：使用 JSON 格式
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * 配置 RabbitTemplate（启用发送确认）
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        
        // 发送消息到交换机的确认回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                // 消息未到达交换机，记录日志并告警
                String msgId = correlationData != null ? correlationData.getId() : "unknown";
                System.err.println("[MQ CONFIRM FAILED] Message not delivered to exchange: " + msgId + ", cause: " + cause);
            }
        });
        
        // 消息路由失败的回调（mandatory=true时触发）
        template.setReturnsCallback(returned -> {
            System.err.println("[MQ RETURN] Message returned: " + returned.getMessage() 
                + ", replyCode: " + returned.getReplyCode() 
                + ", replyText: " + returned.getReplyText());
        });
        
        return template;
    }
}
