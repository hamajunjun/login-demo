package com.example.logindemo.controller;

import com.example.logindemo.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息队列演示：充当"生产者"（发消息的一方）
 */
@RestController
@RequestMapping("/mq")
public class MqDemoController {

    // RabbitTemplate 是 Spring 提供的发消息工具，注入即可用
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/send")
    public String send(@RequestParam String msg) {
        // convertAndSend(交换机名, 路由键, 消息体)
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY, msg);
        return "已发送消息：" + msg;
    }
}