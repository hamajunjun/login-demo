package com.example.logindemo.service;

import java.util.UUID;
import com.example.logindemo.entity.Post;
import com.example.logindemo.entity.PostLike;
import com.example.logindemo.mapper.PostLikeMapper;
import com.example.logindemo.mapper.PostMapper;
import com.example.logindemo.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.logindemo.config.RabbitConfig;
import com.example.logindemo.dto.NotificationMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class PostLikeServiceImpl implements PostLikeService{

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean like(Long postId, Long userId){
        if(postId==null || userId==null){
            throw new RuntimeException("参数不能为空");
        }

        // 先确认帖子存在，再插入点赞，避免给不存在的帖子制造脏数据
        Post post = postMapper.findById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        PostLike postLike=new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);
        try {
            boolean success = postLikeMapper.insert(postLike) > 0;
            if (!success) {
                throw new RuntimeException("点赞失败");
            }

            redisUtil.delete("post:detail:"+postId);
            // 通知帖子作者：改成发消息（异步）
            if(!post.getUserId().equals(userId)){
                NotificationMessage message = new NotificationMessage();
                message.setMessageId(UUID.randomUUID().toString());
                message.setUserId(post.getUserId());
                message.setType("LIKE");
                message.setContent("有人赞了你的帖子《" + post.getTitle() + "》");

                // 关键：注册回调，等事务提交成功后再发消息
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
            return true;
        } catch (DuplicateKeyException e) {
            // 联合唯一索引保证同一用户只能点赞一次。并发或重复请求发生时，
            // 数据库会抛出重复键异常；此时目标状态已经达成，直接按成功处理。
            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlike(Long postId,Long userId){
        if(postId==null || userId==null){
            throw new RuntimeException("参数不能为空");
        }
        int rows = postLikeMapper.delete(postId, userId);
        if (rows == 0) {
            throw new RuntimeException("未点赞该帖子");
        }
        redisUtil.delete("post:detail:" + postId);
        return true;
    }

    @Override
    public int getLikeCount(Long postId){
        if(postId==null){
            return 0;
        }
        return postLikeMapper.countById(postId);
    }

    @Override
    public boolean hasLiked(Long postId, Long userId){
        if(postId==null || userId==null){
            return false;
        }
        return postLikeMapper.findPostIdAndUserId(postId,userId)!=null;
    }
}















