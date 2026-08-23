package com.example.logindemo.service;

import com.example.logindemo.entity.User;
import com.example.logindemo.entity.UserFollow;
import com.example.logindemo.mapper.UserFollowMapper;
import com.example.logindemo.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserFollowServiceImpl implements UserFollowService{

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long userId, Long followingId){
        if(userId.equals(followingId)){
            throw new RuntimeException("不能关注自己");
        }

        User targetUser = userMapper.findById(followingId);
        if(targetUser==null){
            throw new RuntimeException("被关注的用户不存在");
        }

        UserFollow exist=userFollowMapper.findOne(userId, followingId);
        if(exist !=null){
            throw new RuntimeException("已经关注过了");
        }

        UserFollow follow = new UserFollow();
        follow.setFollowerId(userId);
        follow.setFollowingId(followingId);
        userFollowMapper.insert(follow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long userId, Long followingId){
        int result= userFollowMapper.delete(userId, followingId);
        if(result==0){
            throw new RuntimeException("未关注该用户");
        }
    }

    @Override
    public boolean isFollowing(Long userId, Long followingId){
        return userFollowMapper.findOne(userId, followingId)!=null;
    }

    @Override
    public PageInfo<User> listFollowings(Long userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        return new PageInfo<>(userFollowMapper.findFollowingUsers(userId));
    }
    @Override
    public PageInfo<User> listFollowers(Long userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(userFollowMapper.findFollowerUsers(userId));
    }
    @Override
    public int countFollowing(Long userId) {
        return userFollowMapper.countFollowing(userId);
    }

    @Override
    public int countFollowers(Long userId) {
        return userFollowMapper.countFollowers(userId);
    }

}















