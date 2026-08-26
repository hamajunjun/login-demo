package com.example.logindemo.config;

import com.example.logindemo.entity.User;
import com.example.logindemo.mapper.UserMapper;
import com.example.logindemo.util.BloomFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BloomFilterInitRunner implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BloomFilterUtil bloomFilterUtil;

    @Override
    public void run(String... args) {
        // 1. 查询所有用户
        List<User> userList = userMapper.findAll();

        // 2. 提取用户名列表
        List<String> usernames = new ArrayList<>();
        for (User user : userList) {
            usernames.add(user.getUsername());
        }

        // 3. 初始化布隆过滤器
        bloomFilterUtil.init(usernames);

        System.out.println("【布隆过滤器】初始化完成，共加载 " + usernames.size() + " 个用户名");
    }
}