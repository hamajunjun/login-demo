package com.example.logindemo.dto;

import lombok.Data;

/**
 * 一篇帖子对应的点赞数量。
 */
@Data
public class PostLikeCountDTO {
    private Long postId;
    private Integer likeCount;
}
