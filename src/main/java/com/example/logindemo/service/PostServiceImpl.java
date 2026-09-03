package com.example.logindemo.service;

import com.example.logindemo.entity.Post;
import com.example.logindemo.mapper.CommunityMapper;
import com.example.logindemo.mapper.PostMapper;
import com.example.logindemo.util.PostBloomFilterUtil;
import com.example.logindemo.util.RedisUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import com.example.logindemo.mapper.CommentMapper;
import com.example.logindemo.mapper.PostLikeMapper;
import com.example.logindemo.mapper.PostFavoriteMapper;

@Service
public class PostServiceImpl implements PostService{

    private final Random random =new Random();
    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private PostFavoriteMapper postFavoriteMapper;
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostBloomFilterUtil postBloomFilterUtil;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommunityMapper communityMapper;

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createPost(String title, String content,
                              String username, Long userId,Long communityId,Integer rating){
        Post post=new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setUsername(username);
        post.setUserId(userId);
        post.setCommunityId(communityId);
        post.setRating(rating);
        post.setViewCount(0);

        int result=postMapper.insertPost(post);
        if(result>0){
            communityMapper.updateRating(communityId);

            // 发帖成功后，把新帖子 ID 加入布隆过滤器
            postBloomFilterUtil.add(post.getId());
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
        // 0. 先用布隆过滤器判断帖子 ID 是否可能存在
        // 如果不存在，直接返回 null，避免后续所有查询
        if (!postBloomFilterUtil.mightContain(id)) {
            return null;
        }

        // 数据库浏览量+1（这个不需要锁，所有请求都应该计数）
        postMapper.increaseViewCount(id);

        // 1. 先查 Redis 缓存
        String key = "post:detail:" + id;
        Post post = redisUtil.getObject(key, Post.class);
        if (post != null) {
            // 缓存命中，把缓存里的 viewCount 也 +1
            post.setViewCount(post.getViewCount()+1);
            int expireMinutes=30+random.nextInt(10);
            redisUtil.setObject(key,post,expireMinutes,TimeUnit.MINUTES);
            return post;
        }

        // 缓存没有，加分布式锁，防止缓存击穿
        // 锁的 key 和缓存 key 区分开，前面加 "lock:" 前缀
        RLock lock = redissonClient.getLock("lock:" + key);
        lock.lock();
        try {
            // 拿到锁后再查一次，可能别的线程已经重建了缓存
            post=redisUtil.getObject(key,Post.class);
            if (post != null) {
                post.setViewCount(post.getViewCount()+1);
                int expireMinutes=30+random.nextInt(10);
                redisUtil.setObject(key,post,expireMinutes,TimeUnit.MINUTES);
                return post;
            }

            // 2. Redis 没有，查数据库
            post = postMapper.findById(id);

            if (post == null) {
                // 数据库也没有，缓存空值，防止缓存穿透
                int expireMinutes = 5 + random.nextInt(3);
                redisUtil.setObject(key, null, expireMinutes, TimeUnit.MINUTES);
                return null;
            }

            // 3. 填充点赞数
            post.setLikeCount(postLikeService.getLikeCount(id));

            // 4. 存入 Redis
            int expireMinutes = 30 + random.nextInt(10);
            redisUtil.setObject(key, post, expireMinutes, TimeUnit.MINUTES);

            return post;
        } finally {
            // 无论业务成功还是失败，都要释放锁
            lock.unlock();
        }
    }

    @Override
    public boolean updatePost(Long id, String title, String content, Long userId) {
        // 1. 先查出帖子
        Post post = postMapper.findById(id);
        // 2. 帖子不存在
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 3. 判断是不是当前用户发的，按 userId 判断，避免用户改名后丢失权限
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改该帖子");
        }

        // 4. 执行更新
        int result = postMapper.updatePost(id, title, content);
        if (result > 0) {
            redisUtil.delete("post:detail:" + id);
        }
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePost(Long id, Long userId) {
        // 1. 查出帖子
        Post post = postMapper.findById(id);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 2. 判断是不是当前用户发的，按 userId 判断
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该帖子");
        }

        // 3. 删除帖子关联数据
        deletePostData(id);

        // 4. 删除帖子本身
        int result = postMapper.deletePost(id);
        if (result > 0 && post.getCommunityId() != null) {
            communityMapper.updateRating(post.getCommunityId());
        }
        return result > 0;
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
    @Transactional(rollbackFor = Exception.class)
    public boolean adminDeletePost(Long id) {
        Post post = postMapper.findById(id);
        if (post == null) {
            return false;
        }

        deletePostData(id);

        int result = postMapper.deletePost(id);
        if (result > 0 && post.getCommunityId() != null) {
            communityMapper.updateRating(post.getCommunityId());
        }
        return result > 0;
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

    private void deletePostData(Long postId) {
        commentMapper.deleteByPostId(postId);
        postLikeMapper.deleteByPostId(postId);
        postFavoriteMapper.deleteByPostId(postId);
        redisUtil.delete("post:detail:" + postId);
    }
}
