package com.example.logindemo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 通知消息：点赞/评论后，通过消息队列传递的"包裹"
 */
@Data
public class NotificationMessage implements Serializable {

    private String messageId;  // 消息唯一 ID，用于幂等去重
    private Long userId;    // 通知给谁（被点赞的那个作者）
    private String type;    // 通知类型：LIKE / COMMENT / REPLY
    private String content; // 通知内容
}