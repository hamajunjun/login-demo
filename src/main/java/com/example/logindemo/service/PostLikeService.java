package com.example.logindemo.service;

public interface PostLikeService {

    boolean like(Long postId, Long userId);

    boolean unlike(Long postId, Long userId);

    int getLikeCount(Long postId);

    boolean hasLiked(Long postId, Long userId);
}
