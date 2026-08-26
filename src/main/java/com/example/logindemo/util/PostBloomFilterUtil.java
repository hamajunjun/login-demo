package com.example.logindemo.util;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostBloomFilterUtil {

    // 帖子 ID 布隆过滤器在 Redis 里的名字
    private static final String POST_ID_BLOOM_FILTER = "post:id:bloom:filter";

    // 从配置文件读取预计元素数
    @Value("${bloom.filter.post.expected-insertions}")
    private long expectedInsertions;

    // 从配置文件读取误判率
    @Value("${bloom.filter.post.false-probability}")
    private double falseProbability;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 初始化帖子 ID 布隆过滤器
     * 项目启动时调用一次
     */
    public void init(List<Long> postIds) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(POST_ID_BLOOM_FILTER);

        // 如果已经存在，先删除重新初始化
        if (bloomFilter.isExists()) {
            bloomFilter.delete();
        }

        // 初始化
        bloomFilter.tryInit(expectedInsertions, falseProbability);

        // 批量添加帖子 ID
        if (postIds != null && !postIds.isEmpty()) {
            for (Long postId : postIds) {
                bloomFilter.add(postId);
            }
        }
    }

    /**
     * 添加一个帖子 ID
     */
    public void add(Long postId) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(POST_ID_BLOOM_FILTER);
        bloomFilter.add(postId);
    }

    /**
     * 判断帖子 ID 是否可能存在
     */
    public boolean mightContain(Long postId) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(POST_ID_BLOOM_FILTER);
        return bloomFilter.contains(postId);
    }
}