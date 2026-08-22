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
    public boolean sendNotification(Long userId,String type,String content){
        if(userId==null || type==null || type.trim().isEmpty() || content==null || content.trim().isEmpty()){
            return false;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setContent(content.trim());

        return notificationMapper.insert(notification)>0;
    }

    @Override
    public boolean markRead(String token,Long notificationId){
        if(notificationId==null){
            return false;
        }
        User user=getCurrentUser(token);
        return notificationMapper.markRead(notificationId,user.getId())>0;
    }


    @Override
    public PageInfo<Notification> listMyNotification(String token,int pageNum,int pageSize,String type){
        User user=getCurrentUser(token);
        PageHelper.startPage(pageNum,pageSize);
        List<Notification> list;
        if(type !=null && !type.trim().isEmpty()){
            list=notificationMapper.findByUserIdAndType(user.getId(),type);
        }else{
            list=notificationMapper.findByUserId(user.getId());
        }
        return new PageInfo<>(list);
    }

    @Override
    public boolean markAllRead(String token){
        User user=getCurrentUser(token);
        return notificationMapper.markAllRead(user.getId())>=0;
    }

    @Override
    public int countUnread(String token){
        User user=getCurrentUser(token);
        return notificationMapper.countUnread(user.getId());
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
