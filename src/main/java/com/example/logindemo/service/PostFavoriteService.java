package com.example.logindemo.service;

import com.example.logindemo.entity.Post;
import com.github.pagehelper.PageInfo;

public interface PostFavoriteService {

    /**
     * 收藏帖子
     */
    void addFavorite(String token, Long postId);

    /**
     * 取消收藏
     */
    void cancelFavorite(String token, Long postId);

    /**
     * 判断是否已收藏
     */
    boolean isFavorite(String token, Long postId);

    /**
     * 查询我收藏的帖子列表（分页）
     */
    PageInfo<Post> listMyFavorites(String token, int pageNum, int pageSize);
}