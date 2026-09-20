package com.example.logindemo.service;

import com.example.logindemo.entity.Post;
import com.example.logindemo.entity.PostFavorite;
import com.example.logindemo.mapper.PostFavoriteMapper;
import com.example.logindemo.mapper.PostMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostFavoriteServiceImpl implements PostFavoriteService{
    @Autowired
    private PostFavoriteMapper postFavoriteMapper;

    @Autowired
    private PostMapper postMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long userId, Long postId){
        // 先确认帖子存在
        Post post = postMapper.findById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        PostFavorite favorite=new PostFavorite();
        favorite.setUserId(userId);
        favorite.setPostId(postId);

        try {
            int rows = postFavoriteMapper.insert(favorite);
            if (rows != 1) {
                throw new RuntimeException("收藏失败");
            }
        } catch (DuplicateKeyException e) {
            // 联合唯一索引保证同一用户只能收藏一次。重复请求时，
            // 目标状态已达成，按成功处理即可。
            return;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelFavorite(Long userId, Long postId){
        int result = postFavoriteMapper.delete(userId, postId);
        if (result == 0) {
            throw new RuntimeException("未收藏该帖子");
        }
    }

    @Override
    public boolean isFavorite(Long userId, Long postId) {
        PostFavorite favorite = postFavoriteMapper.findByUserIdAndPostId(userId, postId);
        return favorite != null;
    }

    @Override
    public PageInfo<Post> listMyFavorites(Long userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Post> postList = postFavoriteMapper.findFavoritesByUserId(userId);
        return new PageInfo<>(postList);
    }
}
