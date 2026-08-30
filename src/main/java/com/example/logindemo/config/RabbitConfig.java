package com.example.logindemo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;


/**
 * RabbitMQ 配置：声明交换机、队列、以及它们之间的绑定关系
 */
@Configuration
public class RabbitConfig {

    // ===== Hello World 演示用（之前学的） =====
    public static final String EXCHANGE_NAME = "test.direct";
    public static final String QUEUE_NAME = "test.hello";
    public static final String ROUTING_KEY = "hello";

    // ===== 通知（点赞/评论）用，新增 =====
    public static final String NOTIFY_EXCHANGE = "notification.exchange";
    public static final String NOTIFY_QUEUE = "notification.queue";
    public static final String NOTIFY_ROUTING_KEY = "notification";

    public static final String NOTIFY_DLX_EXCHANGE = "notification.dlx.exchange";
    public static final String NOTIFY_DLQ_QUEUE = "notification.dlq";
    public static final String NOTIFY_DLQ_ROUTING_KEY = "notification.dlq";

    // ----- Hello World 的队列/交换机/绑定 -----
    @Bean
    public Queue helloQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange helloExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding helloBinding() {
        return BindingBuilder.bind(helloQueue()).to(helloExchange()).with(ROUTING_KEY);
    }

    // ----- 通知的队列/交换机/绑定（新增，和上面一模一样的套路）-----
    @Bean
    public Queue notifyQueue() {
        // 给队列设置"死信交换机"参数：这条队列产生死信时，送到哪去
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", NOTIFY_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", NOTIFY_DLQ_ROUTING_KEY);

        return new Queue(NOTIFY_QUEUE, true, false, false, args);
    }

    @Bean
    public DirectExchange notifyExchange() {
        return new DirectExchange(NOTIFY_EXCHANGE);
    }

    @Bean
    public Binding notifyBinding() {
        return BindingBuilder.bind(notifyQueue()).to(notifyExchange()).with(NOTIFY_ROUTING_KEY);
    }


    @Bean
    public DirectExchange notifyDlxExchange() {
        return new DirectExchange(NOTIFY_DLX_EXCHANGE);
    }

    @Bean
    public Queue notifyDlq() {
        return new Queue(NOTIFY_DLQ_QUEUE, true);
    }

    @Bean
    public Binding notifyDlqBinding() {
        return BindingBuilder.bind(notifyDlq()).to(notifyDlxExchange()).with(NOTIFY_DLQ_ROUTING_KEY);
    }
    // ----- JSON 转换器（新增）-----
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}