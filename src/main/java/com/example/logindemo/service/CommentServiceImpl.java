package com.example.logindemo.service;

import com.example.logindemo.config.RabbitConfig;
import com.example.logindemo.dto.NotificationMessage;
import com.example.logindemo.entity.Comment;
import com.example.logindemo.entity.Post;
import com.example.logindemo.mapper.CommentMapper;
import com.example.logindemo.mapper.PostMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addComment(Long postId, Long userId, String username, String content, Long parentId) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("评论内容不能为空");
        }

        // 先确认帖子存在，再写评论
        Post post = postMapper.findById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        if (parentId != null) {
            Comment parentComment = commentMapper.findById(parentId);
            if (parentComment == null) {
                throw new RuntimeException("回复的评论不存在");
            }
            if (!parentComment.getPostId().equals(postId)) {
                throw new RuntimeException("只能回复该帖子下的评论");
            }
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setContent(content.trim());
        comment.setParentId(parentId);

        int result = commentMapper.insertComment(comment);
        if (result <= 0) {
            throw new RuntimeException("评论失败");
        }

        // 1. 通知帖子作者（只要不是自己评论自己帖子）
        if (!post.getUserId().equals(userId)) {
            String noticeContent;
            if (parentId == null) {
                noticeContent = username + "评论了你的帖子《" + post.getTitle() + "》：" + content.trim();
            } else {
                noticeContent = username + "回复了你的帖子《" + post.getTitle() + "》：" + content.trim();
            }

            if (noticeContent.length() > 200) {
                noticeContent = noticeContent.substring(0, 200) + "...";
            }

            sendNotificationAfterCommit(post.getUserId(), "COMMENT", noticeContent);
        }

        // 2. 如果是回复，还要通知被回复的人
        if (parentId != null) {
            Comment parentComment = commentMapper.findById(parentId);
            if (parentComment != null && !parentComment.getUserId().equals(userId)) {
                String replyContent = username + "回复了你的评论：" + content.trim();
                if (replyContent.length() > 200) {
                    replyContent = replyContent.substring(0, 200) + "...";
                }
                sendNotificationAfterCommit(parentComment.getUserId(), "REPLY", replyContent);
            }
        }

        return true;
    }

    // 统一通过 RabbitMQ 发通知：等事务提交成功后再发，避免事务回滚了还发通知
    private void sendNotificationAfterCommit(Long userId, String type, String content) {
        NotificationMessage message = new NotificationMessage();
        message.setMessageId(UUID.randomUUID().toString());
        message.setUserId(userId);
        message.setType(type);
        message.setContent(content);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rabbitTemplate.convertAndSend(
                        RabbitConfig.NOTIFY_EXCHANGE,
                        RabbitConfig.NOTIFY_ROUTING_KEY,
                        message
                );
            }
        });
    }

    @Override
    public PageInfo<Comment> listByPostId(Long postId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Comment> list = commentMapper.findByPostId(postId);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long id, Long currentUserId, String currentRole) {
        Comment comment = commentMapper.findById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 只有评论作者或管理员可以删除，按 userId 判断
        if (!comment.getUserId().equals(currentUserId) && !"ADMIN".equals(currentRole)) {
            throw new RuntimeException("无权删除该评论");
        }

        // 先删除这条评论下面的回复
        commentMapper.deleteByParentId(id);

        // 再删除这条评论本身
        int result = commentMapper.deleteById(id);
        if (result <= 0) {
            throw new RuntimeException("删除失败");
        }

        return true;
    }
}
