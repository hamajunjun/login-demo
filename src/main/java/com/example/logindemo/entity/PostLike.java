package com.example.logindemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostLike {
    private Long id;
    private Long postId;
    private Long userId;
    private LocalDateTime createTime;
}
