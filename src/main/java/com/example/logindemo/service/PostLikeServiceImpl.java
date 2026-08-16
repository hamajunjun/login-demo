package com.example.logindemo.service;

import com.example.logindemo.entity.PostLike;
import com.example.logindemo.mapper.PostLikeMapper;
import com.example.logindemo.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostLikeServiceImpl implements PostLikeService{

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean like(Long postId, Long userId){
        if(postId==null || userId==null){
            return false;
        }
        PostLike exist = postLikeMapper.findPostIdAndUserId(postId,userId);
        if(exist !=null){
            return false;
        }
        PostLike postLike=new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);
        boolean success=postLikeMapper.insert(postLike)>0;
        if(success){
            redisUtil.delete("post:detail:"+postId);
        }
        return success;
    }

    @Override
    public boolean unlike(Long postId,Long userId){
        if(postId==null || userId==null){
            return false;
        }
        PostLike exist=postLikeMapper.findPostIdAndUserId(postId,userId);
        if(exist==null){
            return false;
        }
        boolean success= postLikeMapper.delete(postId,userId)>0;
        if(success){
            redisUtil.delete("post:detail:"+postId);
        }
        return success;
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















