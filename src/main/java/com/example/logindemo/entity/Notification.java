package com.example.logindemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notification {
    private Long id;
    private Long userId;
    private String type;
    private String content;
    private Integer isRead;
    private LocalDateTime createTime;
}