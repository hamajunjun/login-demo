package com.example.logindemo.service;

import com.example.logindemo.entity.Notification;
import com.example.logindemo.mapper.NotificationMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService{

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public boolean sendNotification(Long userId,String type,String content){
        if(userId==null || type==null || type.trim().isEmpty() || content==null || content.trim().isEmpty()){
            throw new RuntimeException("通知参数不完整");
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setContent(content.trim());

        boolean success = notificationMapper.insert(notification)>0;
        if (!success) {
            throw new RuntimeException("通知发送失败");
        }
        return true;
    }

    @Override
    public boolean markRead(Long userId,Long notificationId){
        if(notificationId==null){
            throw new RuntimeException("通知ID不能为空");
        }
        boolean success = notificationMapper.markRead(notificationId, userId)>0;
        if (!success) {
            throw new RuntimeException("通知不存在或无权限");
        }
        return true;
    }


    @Override
    public PageInfo<Notification> listMyNotification(Long userId,int pageNum,int pageSize,String type){
        PageHelper.startPage(pageNum,pageSize);
        List<Notification> list;
        if(type !=null && !type.trim().isEmpty()){
            list=notificationMapper.findByUserIdAndType(userId, type);
        }else{
            list=notificationMapper.findByUserId(userId);
        }
        return new PageInfo<>(list);
    }

    @Override
    public boolean markAllRead(Long userId){
        return notificationMapper.markAllRead(userId)>=0;
    }

    @Override
    public int countUnread(Long userId){
        return notificationMapper.countUnread(userId);
    }
}
