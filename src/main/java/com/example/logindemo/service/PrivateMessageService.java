package com.example.logindemo.service;

import com.example.logindemo.entity.PrivateMessage;
import com.github.pagehelper.PageInfo;

public interface PrivateMessageService {
    /**
     * 发送私信
     */
    boolean sendMessage(Long senderId,Long receiverId,String content);
    /**
     * 查询收件箱
     */
    PageInfo<PrivateMessage> listInbox(Long userId, int pageNum, int pageSize);
    /**
     * 查询发件箱
     */
    PageInfo<PrivateMessage> listOutbox(Long userId, int pageNum, int pageSize);
    /**
     * 查询未读私信数
     */
    int countUnread(Long userId);
    /**
     * 标记私信已读
     */
    boolean markRead(Long userId, Long id);
}
