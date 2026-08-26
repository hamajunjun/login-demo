package com.example.logindemo.config;

import com.example.logindemo.entity.Post;
import com.example.logindemo.mapper.PostMapper;
import com.example.logindemo.util.PostBloomFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PostBloomFilterInitRunner implements CommandLineRunner {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostBloomFilterUtil postBloomFilterUtil;

    @Override
    public void run(String... args) {
        // 1. 查询所有帖子
        List<Post> postList = postMapper.findAll();

        // 2. 提取帖子 ID 列表
        List<Long> postIds = new ArrayList<>();
        for (Post post : postList) {
            postIds.add(post.getId());
        }

        // 3. 初始化帖子 ID 布隆过滤器
        postBloomFilterUtil.init(postIds);

        System.out.println("【帖子 ID 布隆过滤器】初始化完成，共加载 " + postIds.size() + " 个帖子 ID");
    }
}