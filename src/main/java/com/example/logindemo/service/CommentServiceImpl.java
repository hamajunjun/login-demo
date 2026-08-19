package com.example.logindemo.service;

import com.example.logindemo.entity.Comment;
import com.example.logindemo.entity.Post;
import com.example.logindemo.mapper.CommentMapper;
import com.example.logindemo.mapper.PostMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private NotificationService notificationService;

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
        if(result <= 0){
            return false;
        }
        //查询帖子信息
        Post post=postMapper.findById(postId);
        if(post==null){
            return true;// 评论插入成功，但帖子找不到，不发通知
        }
        // 1. 通知帖子作者（只要不是自己评论自己帖子）
        if(!post.getUserId().equals(userId)){
            String noticeContent;
            if (parentId == null) {
                noticeContent = username + "评论了你的帖子《" + post.getTitle() + "》：" + content.trim();
            } else {
                noticeContent = username + "回复了你的帖子《" + post.getTitle() + "》：" + content.trim();
            }
            // 内容太长截断一下
            if(noticeContent.length()>200){
                noticeContent=noticeContent.substring(0,200)+"...";
            }
            notificationService.sendNotification(post.getUserId(),"COMMENT",noticeContent);
        }
        // 2. 如果是回复，还要通知被回复的人
        if(parentId!=null){
            Comment parentComment=commentMapper.findById(parentId);
            if(parentComment!=null && !parentComment.getUserId().equals(userId)){
                String replyContent=username+"回复了你的评论："+content.trim();
                if (replyContent.length() > 200) {
                    replyContent = replyContent.substring(0, 200) + "...";
                }
                notificationService.sendNotification(parentComment.getUserId(),"REPLY", replyContent);
            }
        }
        return true;
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