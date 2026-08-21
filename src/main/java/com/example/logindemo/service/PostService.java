package com.example.logindemo.service;

import com.example.logindemo.entity.Post;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface PostService {

    boolean createPost(String title,String content, String username,Long userId,Long communityId,Integer rating);

    PageInfo<Post> listPosts(int pageNum,int pageSize);

    Post getPostById(Long id);

    boolean updatePost(Long id,String title,String content,String username);

    boolean deletePost(Long id,String username);

    PageInfo<Post> listByCommunityId(Long communityId,int pageNum,int pageSize);

    PageInfo<Post> listByUserId(Long userId,int pageNum,int pageSize);

    boolean adminDeletePost(Long id);

    PageInfo<Post> findByKeyword(String keyword,int pageNum,int pageSize);

    PageInfo<Post> getHotList(int pageNum,int pageSize);
}

