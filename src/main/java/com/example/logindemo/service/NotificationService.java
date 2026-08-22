package com.example.logindemo.service;

import com.example.logindemo.entity.Notification;
import com.github.pagehelper.PageInfo;

public interface NotificationService {
    /**
     * 发送一条通知
     */
    boolean sendNotification(Long userId,String type,String content);
    /**
     * 标记通知为已读
     */
    boolean markRead(String token,Long notificationId);
    /**
     * 查询未读通知数
     */
    int countUnread(String token);

    PageInfo<Notification> listMyNotification(String token,int pageNum,int pageSize,String type);

    boolean markAllRead(String token);


}
