package com.example.logindemo.util;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BloomFilterUtil {

    // 布隆过滤器在 Redis 里的名字
    private static final String USERNAME_BLOOM_FILTER = "username:bloom:filter";

    // 从配置文件读取预计元素数
    @Value("${bloom.filter.username.expected-insertions}")
    private long expectedInsertions;

    // 从配置文件读取误判率
    @Value("${bloom.filter.username.false-probability}")
    private double falseProbability;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 初始化布隆过滤器
     * 项目启动时调用一次，把所有已存在的用户名加进去
     */
    public void init(List<String> usernames) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(USERNAME_BLOOM_FILTER);

        // 如果 Redis 里已经存在这个布隆过滤器，先删掉，重新初始化
        if (bloomFilter.isExists()) {
            bloomFilter.delete();
        }

        // 初始化布隆过滤器：使用配置文件里的参数
        bloomFilter.tryInit(expectedInsertions, falseProbability);

        // 批量添加用户名
        if (usernames != null && !usernames.isEmpty()) {
            for (String username : usernames) {
                bloomFilter.add(username);
            }
        }
    }

    /**
     * 添加一个用户名到布隆过滤器
     */
    public void add(String username) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(USERNAME_BLOOM_FILTER);
        bloomFilter.add(username);
    }

    /**
     * 判断用户名是否可能存在
     * @return true: 可能存在  false: 肯定不存在
     */
    public boolean mightContain(String username) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(USERNAME_BLOOM_FILTER);
        return bloomFilter.contains(username);
    }
}