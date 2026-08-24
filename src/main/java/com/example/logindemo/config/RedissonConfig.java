package com.example.logindemo.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    // 从 application.properties 里读取 Redis 配置
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    // : 后面是默认值，如果配置文件里没有 password，就默认空字符串
    @Value("${spring.redis.password:}")
    private String password;

    // 把这个对象交给 Spring 管理
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        // 单节点 Redis 模式
        // 拼接地址：redis://localhost:6379
        String address = "redis://" + host + ":" + port;

        if (password != null && !password.isEmpty()) {
            // 有密码时，把密码告诉 Redisson
            config.useSingleServer()
                    .setAddress(address)
                    .setPassword(password);
        } else {
            // 没有密码时，只设置地址，不设置密码
            config.useSingleServer()
                    .setAddress(address);
        }

        return Redisson.create(config);
    }
}