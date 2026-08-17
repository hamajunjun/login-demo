package com.example.logindemo.service;

import com.example.logindemo.entity.Post;
import com.example.logindemo.entity.PostFavorite;
import com.example.logindemo.entity.User;
import com.example.logindemo.mapper.PostFavoriteMapper;
import com.example.logindemo.mapper.PostMapper;
import com.example.logindemo.mapper.UserMapper;
import com.example.logindemo.service.PostFavoriteService;
import com.example.logindemo.util.JwtUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostFavoriteServiceImpl implements PostFavoriteService{
    @Autowired
    private PostFavoriteMapper postFavoriteMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Override
    public void addFavorite(String token,Long postId){
        String username=JwtUtil.getUsername(token);

        User user=userMapper.findByUsername(username);
        if(user==null){
            throw new RuntimeException("用户不存在");
        }

        PostFavorite favorite=new PostFavorite();
        favorite.setUserId(user.getId());
        favorite.setPostId(postId);

        postFavoriteMapper.insert(favorite);
    }

    @Override
    public void cancelFavorite(String token,Long postId){
        String username=JwtUtil.getUsername(token);
        User user=userMapper.findByUsername(username);
        if(user==null){
            throw new RuntimeException("用户不存在");
        }
        postFavoriteMapper.delete(user.getId(), postId);
    }

    @Override
    public boolean isFavorite(String token, Long postId) {
        String username = JwtUtil.getUsername(token);
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return false;
        }

        PostFavorite favorite = postFavoriteMapper.findByUserIdAndPostId(user.getId(), postId);
        return favorite != null;
    }

    @Override
    public PageInfo<Post> listMyFavorites(String token,int pageNum,int pageSize){
        String username=JwtUtil.getUsername(token);
        User user=userMapper.findByUsername(username);
        if(user==null){
            throw new RuntimeException("用户不存在");
        }
        PageHelper.startPage(pageNum,pageSize);
        List<PostFavorite> favoriteList = postFavoriteMapper.findByUserId(user.getId());

        List<Post> postList=new ArrayList<>();
        for(PostFavorite favorite:favoriteList){
            Post post = postMapper.findById(favorite.getPostId());
            if (post != null) {
                postList.add(post);
            }
        }
        return new PageInfo<>(postList);
    }
}
