package com.example.logindemo.listener;

import com.example.logindemo.config.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 消息队列演示：充当"消费者"（收消息的一方）
 */
@Component
public class HelloListener {

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void onMessage(String msg) {
        // 收到消息后打印出来。真实项目里，这里就是你的业务逻辑
        System.out.println("【消费者收到消息】" + msg);
    }
}