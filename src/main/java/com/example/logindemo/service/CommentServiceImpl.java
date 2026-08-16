package com.example.logindemo.service;

import com.example.logindemo.entity.Comment;
import com.example.logindemo.mapper.CommentMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public boolean addComment(Long postId, Long userId, String username, String content, Long parentId) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setContent(content.trim());
        comment.setParentId(parentId);

        int result = commentMapper.insertComment(comment);
        return result > 0;
    }

    @Override
    public PageInfo<Comment> listByPostId(Long postId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Comment> list = commentMapper.findByPostId(postId);
        return new PageInfo<>(list);
    }

    @Override
    public boolean deleteComment(Long id, String currentUsername, String currentRole) {
        Comment comment = commentMapper.findById(id);
        if (comment == null) {
            return false;
        }

        // 只有评论作者或管理员可以删除
        if (!comment.getUsername().equals(currentUsername) && !"ADMIN".equals(currentRole)) {
            return false;
        }

        int result = commentMapper.deleteById(id);
        return result > 0;
    }
}