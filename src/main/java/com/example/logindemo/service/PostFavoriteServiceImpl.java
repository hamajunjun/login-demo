package com.example.logindemo.service;

import com.example.logindemo.entity.Post;
import com.example.logindemo.entity.PostFavorite;
import com.example.logindemo.mapper.PostFavoriteMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostFavoriteServiceImpl implements PostFavoriteService{
    @Autowired
    private PostFavoriteMapper postFavoriteMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long userId, Long postId){
        PostFavorite favorite=new PostFavorite();
        favorite.setUserId(userId);
        favorite.setPostId(postId);

        postFavoriteMapper.insert(favorite);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelFavorite(Long userId, Long postId){
        postFavoriteMapper.delete(userId, postId);
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
