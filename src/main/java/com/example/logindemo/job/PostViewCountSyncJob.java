package com.example.logindemo.job;

import com.example.logindemo.mapper.PostMapper;
import com.example.logindemo.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PostViewCountSyncJob {

    // Redis 中记录单篇帖子累计浏览量的 key 前缀
    private static final String VIEW_COUNT_KEY_PREFIX = "post:view:";

    // Redis 中记录“哪些帖子需要同步”的 Set 集合
    private static final String PENDING_POST_SET_KEY = "post:view:pending";

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PostMapper postMapper;

    /**
     * 每隔 60 秒，把 Redis 中累计的浏览量写回 MySQL。
     */
    @Scheduled(fixedDelay = 60000)
    public void syncViewCounts() {
        while (true) {
            // 1. 取出一个需要同步浏览量的帖子 ID
            String postIdText = redisUtil.popFromSet(PENDING_POST_SET_KEY);

            // Set 已空，结束本次同步
            if (postIdText == null) {
                return;
            }

            Long postId = Long.valueOf(postIdText);
            String viewKey = VIEW_COUNT_KEY_PREFIX + postId;

            // 2. 取出累计浏览量，并删除 Redis key，避免重复写入
            String countText = redisUtil.getAndDelete(viewKey);

            // 当前帖子没有待同步浏览量，处理下一个
            if (countText == null) {
                continue;
            }

            long count = Long.parseLong(countText);

            try {
                // 3. 一次性把累计浏览量加到 MySQL
                int rows = postMapper.increaseViewCountBy(postId, count);

                if (rows != 1) {
                    System.err.println("帖子不存在，跳过浏览量同步，postId=" + postId);
                    continue;
                }

                // 4. 删除帖子详情缓存，让下次查询从 MySQL 获取最新浏览量
                redisUtil.delete("post:detail:" + postId);

            } catch (Exception e) {
                // MySQL 更新失败：把浏览量加回 Redis，等待下次重试
                redisUtil.increment(viewKey, count);
                redisUtil.addToSet(PENDING_POST_SET_KEY, postIdText);

                System.err.println("浏览量同步失败，postId=" + postId);
            }
        }
    }
}