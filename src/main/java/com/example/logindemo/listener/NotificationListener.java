package com.example.logindemo.listener;

import com.example.logindemo.config.RabbitConfig;
import com.example.logindemo.dto.NotificationMessage;
import com.example.logindemo.service.NotificationService;
import com.example.logindemo.util.RedisUtil;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import com.example.logindemo.websocket.NotificationWebSocketHandler;

/**
 * 通知消费者：监听通知队列，收到消息后写通知（含幂等去重）
 */
@Component
public class NotificationListener {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private NotificationWebSocketHandler notificationWebSocketHandler;

    @RabbitListener(queues = RabbitConfig.NOTIFY_QUEUE)
    public void onNotification(NotificationMessage message) {

        // ===== 幂等去重：靠 messageId 判断这条消息是不是处理过了 =====
        String dedupKey = "mq:consumed:" + message.getMessageId();
        boolean firstTime = redisUtil.setIfAbsent(dedupKey, "1", 24, TimeUnit.HOURS);
        if (!firstTime) {
            // 这个 messageId 之前处理过了 → 重复消息，直接丢弃
            System.out.println("【重复消息，跳过】" + message.getMessageId());
            return;
        }

        try {
            // 正常处理：写通知
            notificationService.sendNotification(
                    message.getUserId(),
                    message.getType(),
                    message.getContent()
            );
            notificationWebSocketHandler.pushToUser(
                    message.getUserId(),
                    message.getContent()
            );
            System.out.println("【异步写通知】类型=" + message.getType() + "，接收用户=" + message.getUserId());

        } catch (Exception e) {
            // 处理失败：拒绝且不重投，送进死信队列
            System.err.println("【通知处理失败，送进死信队列】原因：" + e.getMessage());
            throw new AmqpRejectAndDontRequeueException("通知处理失败", e);
        }
    }
}