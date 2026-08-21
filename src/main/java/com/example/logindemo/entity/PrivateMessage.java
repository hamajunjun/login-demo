package com.example.logindemo.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PrivateMessage {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Integer isRead;
    private LocalDateTime createTime;
    private String senderUsername;
    private String receiverUsername;
}
