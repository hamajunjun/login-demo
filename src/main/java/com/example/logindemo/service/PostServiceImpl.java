package com.example.logindemo.service;

import com.example.logindemo.entity.Post;
import com.example.logindemo.mapper.CommunityMapper;
import com.example.logindemo.mapper.PostMapper;
import com.example.logindemo.util.RedisUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PostServiceImpl implements PostService{
    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommunityMapper communityMapper;

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean createPost(String title, String content,
                              String username, Long userId,Long communityId,Integer rating){
        Post post=new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setUsername(username);
        post.setUserId(userId);
        post.setCommunityId(communityId);
        post.setRating(rating);

        int result=postMapper.insertPost(post);
        if(result>0){
            communityMapper.updateRating(communityId);
        }
        return result>0;
    }

    @Override
    public PageInfo<Post> listPosts(int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Post> list=postMapper.findAll();
        for(Post post:list){
            post.setLikeCount(postLikeService.getLikeCount(post.getId()));
        }
        return new PageInfo<>(list);
    }

    @Override
    public Post getPostById(Long id){
        //数据库浏览量+1
        postMapper.increaseViewCount(id);
        // 1. 先查 Redis 缓存
        String key = "post:detail:" + id;
        Post post = redisUtil.getObject(key, Post.class);
        if (post != null) {
            // 缓存命中，把缓存里的 viewCount 也 +1，这样前端能实时看到
            post.setViewCount(post.getViewCount()+1);
            redisUtil.setObject(key,post,30,TimeUnit.MINUTES);
            return post;
        }

        // 2. Redis 没有，查数据库
        post = postMapper.findById(id);
        if (post == null) {
            return null;
        }
        // 3. 填充点赞数
        post.setLikeCount(postLikeService.getLikeCount(id));

        // 4. 存入 Redis，过期时间 30 分钟
        redisUtil.setObject(key, post, 30, TimeUnit.MINUTES);

        return post;
    }

    @Override
    public boolean updatePost(Long id,String title,String content,String username){
        // 1. 先查出帖子
        Post post = postMapper.findById(id);
        // 2.帖子不存在
        if(post==null){
            return false;
        }
        // 3. 判断是不是当前用户发的
        if(!post.getUsername().equals(username)){
            return false;
        }
        // 4. 执行更新
        int result = postMapper.updatePost(id,title,content);
        return result>0;
    }

    @Override
    public boolean deletePost(Long id,String username){
        // 1. 查出帖子
        Post post=postMapper.findById(id);
        // 2. 帖子不存在
        if(post==null){
            return false;
        }
        // 3. 判断是不是当前用户发的
        if(!post.getUsername().equals(username)){
            return false;
        }
        // 4. 执行删除
        int result = postMapper.deletePost(id);
        return result>0;

    }
    @Override
    public PageInfo<Post> listByCommunityId(Long communityId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Post> list=postMapper.findByCommunityId(communityId);
        return new PageInfo<>(list);
    }
    @Override
    public PageInfo<Post> listByUserId(Long userId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Post> list=postMapper.findByUserId(userId);
        for (Post post : list) {
            post.setLikeCount(postLikeService.getLikeCount(post.getId()));
        }
        return new PageInfo<>(list);
    }

    @Override
    public boolean adminDeletePost(Long id){
        Post post=postMapper.findById(id);
        if(post==null){
            return false;
        }
        int result=postMapper.deletePost(id);
        return result>0;
    }

    @Override
    public PageInfo<Post> findByKeyword(String keyword,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Post> list=postMapper.findByKeyword(keyword);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Post> getHotList(int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Post> list=postMapper.findHotList();
        for(Post post : list){
            post.setLikeCount(postLikeService.getLikeCount(post.getId()));
        }
        return new PageInfo<>(list);
    }
}
