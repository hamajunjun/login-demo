package com.example.logindemo.service;

import com.example.logindemo.entity.User;
import com.example.logindemo.entity.UserFollow;
import com.example.logindemo.mapper.UserFollowMapper;
import com.example.logindemo.mapper.UserMapper;
import com.example.logindemo.util.JwtUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserFollowServiceImpl implements UserFollowService{

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 从 token 里解析当前登录用户
     */
    private User getCurrentUser(String token){
        String username=JwtUtil.getUsername(token);
        User user=userMapper.findByUsername(username);
        if(user==null){
            throw new RuntimeException("用户不存在");
        }
        return user;
    }
    @Override
    public void follow(String token,Long followingId){
        User currentUser=getCurrentUser(token);

        if(currentUser.getId().equals(followingId)){
            throw new RuntimeException("不能关注自己");
        }

        User targetUser = userMapper.findById(followingId);
        if(targetUser==null){
            throw new RuntimeException("被关注的用户不存在");
        }

        UserFollow exist=userFollowMapper.findOne(currentUser.getId(),followingId);
        if(exist !=null){
            throw new RuntimeException("已经关注过了");
        }

        UserFollow follow = new UserFollow();
        follow.setFollowerId(currentUser.getId());
        follow.setFollowingId(followingId);
        userFollowMapper.insert(follow);
    }

    @Override
    public void unfollow(String token,Long followingId){
        User currentUser=getCurrentUser(token);

        int result= userFollowMapper.delete(currentUser.getId(),followingId);
        if(result==0){
            throw new RuntimeException("未关注该用户");
        }
    }

    @Override
    public boolean isFollowing(String token,Long followingId){
        User currentUser = getCurrentUser(token);
        return userFollowMapper.findOne(currentUser.getId(),followingId)!=null;
    }

    @Override
    public PageInfo<User> listFollowings(String token, int pageNum, int pageSize){
        User currentUser = getCurrentUser(token);
        PageHelper.startPage(pageNum,pageSize);
        return new PageInfo<>(userFollowMapper.findFollowingUsers(currentUser.getId()));
    }
    @Override
    public PageInfo<User> listFollowers(String token, int pageNum, int pageSize) {
        User currentUser = getCurrentUser(token);
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(userFollowMapper.findFollowerUsers(currentUser.getId()));
    }
    @Override
    public int countFollowing(String token) {
        User currentUser = getCurrentUser(token);
        return userFollowMapper.countFollowing(currentUser.getId());
    }

    @Override
    public int countFollowers(String token) {
        User currentUser = getCurrentUser(token);
        return userFollowMapper.countFollowers(currentUser.getId());
    }

}















