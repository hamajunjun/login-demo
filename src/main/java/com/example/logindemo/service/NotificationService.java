package com.example.logindemo.service;

import com.example.logindemo.entity.Notification;
import com.github.pagehelper.PageInfo;

public interface NotificationService {
    /**
     * 查询我的通知列表
     */
    PageInfo<Notification> listMyNotification(String token,int pageNum,int pageSize);
}
