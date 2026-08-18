package com.example.logindemo.service;

import com.example.logindemo.entity.Notification;
import com.example.logindemo.entity.User;
import com.example.logindemo.mapper.NotificationMapper;
import com.example.logindemo.mapper.UserMapper;
import com.example.logindemo.util.JwtUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService{

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageInfo<Notification> listMyNotification(String token,int pageNum,int pageSize){
        User user=getCurrentUser(token);
        PageHelper.startPage(pageNum,pageSize);
        List<Notification> list=notificationMapper.findByUserId(user.getId());
        return new PageInfo<>(list);
    }
    private User getCurrentUser(String token){
        String username=JwtUtil.getUsername(token);
        User user=userMapper.findByUsername(username);
        if(user==null){
            throw new RuntimeException("用户不存在");
        }
        return user;
    }
}
