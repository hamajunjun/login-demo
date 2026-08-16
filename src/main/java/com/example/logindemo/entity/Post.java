package com.example.logindemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Post {
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private String username;
    private Long communityId;
    private Integer rating;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer likeCount;
}
