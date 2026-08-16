package com.example.logindemo.service;

import com.example.logindemo.entity.Comment;
import com.github.pagehelper.PageInfo;

public interface CommentService {

    boolean addComment(Long postId, Long userId, String username, String content, Long parentId);

    PageInfo<Comment> listByPostId(Long postId, int pageNum, int pageSize);

    boolean deleteComment(Long id, String currentUsername, String currentRole);
}