package com.example.logindemo.job;

import com.example.logindemo.entity.Post;
import com.example.logindemo.entity.User;
import com.example.logindemo.mapper.PostMapper;
import com.example.logindemo.mapper.UserMapper;
import com.example.logindemo.util.BloomFilterUtil;
import com.example.logindemo.util.PostBloomFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BloomFilterRebuildJob {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private BloomFilterUtil bloomFilterUtil;

    @Autowired
    private PostBloomFilterUtil postBloomFilterUtil;

    /**
     * 每天凌晨 3 点重建布隆过滤器
     * cron 表达式：秒 分 时 日 月 周
     * 0 0 3 * * ? 表示每天凌晨 3 点 0 分 0 秒执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void rebuildBloomFilters() {
        System.out.println("【定时任务】开始重建布隆过滤器...");

        // 1. 重建用户名布隆过滤器
        rebuildUsernameBloomFilter();

        // 2. 重建帖子 ID 布隆过滤器
        rebuildPostIdBloomFilter();

        System.out.println("【定时任务】布隆过滤器重建完成");
    }

    private void rebuildUsernameBloomFilter() {
        // 查询所有用户
        List<User> userList = userMapper.findAll();

        // 提取用户名
        List<String> usernames = new ArrayList<>();
        for (User user : userList) {
            usernames.add(user.getUsername());
        }

        // 重建布隆过滤器
        bloomFilterUtil.init(usernames);

        System.out.println("【定时任务】用户名布隆过滤器重建完成，共 " + usernames.size() + " 个用户名");
    }

    private void rebuildPostIdBloomFilter() {
        // 查询所有帖子
        List<Post> postList = postMapper.findAll();

        // 提取帖子 ID
        List<Long> postIds = new ArrayList<>();
        for (Post post : postList) {
            postIds.add(post.getId());
        }

        // 重建布隆过滤器
        postBloomFilterUtil.init(postIds);

        System.out.println("【定时任务】帖子 ID 布隆过滤器重建完成，共 " + postIds.size() + " 个帖子 ID");
    }
}