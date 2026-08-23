package com.example.logindemo.service;

import com.example.logindemo.entity.PrivateMessage;
import com.example.logindemo.entity.User;
import com.example.logindemo.mapper.PrivateMessageMapper;
import com.example.logindemo.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrivateMessageServiceImpl implements PrivateMessageService{
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PrivateMessageMapper privateMessageMapper;

    @Override
    public boolean sendMessage(Long senderId,Long receiverId,String content){
        if(receiverId==null){
            throw new RuntimeException("接收人不能为空");
        }
        if(senderId.equals(receiverId)){
            throw new RuntimeException("不能给自己发私信");
        }
        User receiver=userMapper.findById(receiverId);
        if(receiver==null){
            throw new RuntimeException("接收人不存在");
        }
        if(content==null || content.trim().isEmpty()){
            throw new RuntimeException("私信内容不能为空");
        }
        PrivateMessage message = new PrivateMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content.trim());

        boolean success = privateMessageMapper.insert(message) > 0;
        if (!success) {
            throw new RuntimeException("发送失败");
        }
        return true;
    }

    @Override
    public PageInfo<PrivateMessage> listInbox(Long userId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<PrivateMessage> list=privateMessageMapper.findInboxByReceiverId(userId);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<PrivateMessage> listOutbox(Long userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<PrivateMessage> list=privateMessageMapper.findOutboxBySenderId(userId);
        return new PageInfo<>(list);
    }
    @Override
    public int countUnread(Long userId){
        return privateMessageMapper.countUnread(userId);
    }

    @Override
    public boolean markRead(Long userId, Long id) {
        if (id == null) {
            throw new RuntimeException("消息ID不能为空");
        }
        return privateMessageMapper.markRead(id, userId) > 0;
    }
}
