package com.example.logindemo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 存字符串
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // 存字符串，带过期时间
    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // 取字符串
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 删除 key
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // 判断 key 是否存在
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // 存对象（自动转 JSON）
    public void setObject(String key, Object value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            set(key, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 序列化失败", e);
        }
    }

    // 存对象，带过期时间
    public void setObject(String key, Object value, long timeout, TimeUnit unit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            set(key, json, timeout, unit);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 序列化失败", e);
        }
    }

    // 取对象（JSON 转对象）
    public <T> T getObject(String key, Class<T> clazz) {
        String json = get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 反序列化失败", e);
        }
    }
}