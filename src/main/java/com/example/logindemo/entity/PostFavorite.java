package com.example.logindemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostFavorite {
    private Long id;
    private Long userId;
    private Long postId;
    private LocalDateTime createTime;
}