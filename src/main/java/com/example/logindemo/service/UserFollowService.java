package com.example.logindemo.service;

import com.example.logindemo.entity.User;
import com.github.pagehelper.PageInfo;

public interface UserFollowService {

    /**
     * 关注用户
     */
    void follow(Long userId, Long followingId);

    /**
     * 取消关注
     */
    void unfollow(Long userId, Long followingId);

    /**
     * 判断是否已关注
     */
    boolean isFollowing(Long userId, Long followingId);

    /**
     * 查询我关注的人
     */
    PageInfo<User> listFollowings(Long userId, int pageNum, int pageSize);

    /**
     * 查询我的粉丝
     */
    PageInfo<User> listFollowers(Long userId, int pageNum, int pageSize);

    /**
     * 统计关注数
     */
    int countFollowing(Long userId);

    /**
     * 统计粉丝数
     */
    int countFollowers(Long userId);
}