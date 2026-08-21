package com.example.logindemo.service;

import com.example.logindemo.entity.PrivateMessage;
import com.github.pagehelper.PageInfo;

public interface PrivateMessageService {
    /**
     * 发送私信
     */
    boolean sendMessage(String token,Long receiverId,String content);
    /**
     * 查询收件箱
     */
    PageInfo<PrivateMessage> listInbox(String token, int pageNum, int pageSize);
    /**
     * 查询发件箱
     */
    PageInfo<PrivateMessage> listOutbox(String token, int pageNum, int pageSize);
    /**
     * 查询未读私信数
     */
    int countUnread(String token);
    /**
     * 标记私信已读
     */
    boolean markRead(String token, Long id);
}
