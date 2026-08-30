package com.example.logindemo.listener;

import com.example.logindemo.config.RabbitConfig;
import com.example.logindemo.dto.NotificationMessage;
import com.example.logindemo.service.NotificationService;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 通知消费者：监听通知队列，收到消息后真正把通知写进数据库
 */
@Component
public class NotificationListener {

    @Autowired
    private NotificationService notificationService;

    @RabbitListener(queues = RabbitConfig.NOTIFY_QUEUE)
    public void onNotification(NotificationMessage message) {
        try {
            // 正常处理：写通知
            notificationService.sendNotification(
                    message.getUserId(),
                    message.getType(),
                    message.getContent()
            );
            System.out.println("【异步写通知】类型=" + message.getType() + "，接收用户=" + message.getUserId());

        } catch (Exception e) {
            // 处理失败：拒绝这条消息且不重投，让它进死信队列
            System.err.println("【通知处理失败，送进死信队列】原因：" + e.getMessage());
            throw new AmqpRejectAndDontRequeueException("通知处理失败", e);
        }
    }
}