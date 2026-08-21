package com.example.logindemo.service;

import com.example.logindemo.entity.PrivateMessage;
import com.example.logindemo.entity.User;
import com.example.logindemo.mapper.PrivateMessageMapper;
import com.example.logindemo.mapper.UserMapper;
import com.example.logindemo.util.JwtUtil;
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
    public boolean sendMessage(String token,Long receiverId,String content){
        String username=JwtUtil.getUsername(token);
        User sender=userMapper.findByUsername(username);
        if(sender==null){
            throw new RuntimeException("发送人不存在");
        }
        if(receiverId==null){
            throw new RuntimeException("接收人不能为空");
        }
        if(sender.getId().equals(receiverId)){
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
        message.setSenderId(sender.getId());
        message.setReceiverId(receiverId);
        message.setContent(content.trim());

        return privateMessageMapper.insert(message)>0;
    }

    @Override
    public PageInfo<PrivateMessage> listInbox(String token,int pageNum,int pageSize){
        User user=getCurrentUser(token);
        PageHelper.startPage(pageNum,pageSize);
        List<PrivateMessage> list=privateMessageMapper.findInboxByReceiverId(user.getId());
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<PrivateMessage> listOutbox(String token, int pageNum, int pageSize){
        User user=getCurrentUser(token);
        PageHelper.startPage(pageNum,pageSize);
        List<PrivateMessage> list=privateMessageMapper.findOutboxBySenderId(user.getId());
        return new PageInfo<>(list);
    }
    @Override
    public int countUnread(String token){
        User user=getCurrentUser(token);
        return privateMessageMapper.countUnread(user.getId());
    }

    @Override
    public boolean markRead(String token, Long id) {
        if (id == null) {
            throw new RuntimeException("消息ID不能为空");
        }
        User user = getCurrentUser(token);
        return privateMessageMapper.markRead(id, user.getId()) > 0;
    }

    private User getCurrentUser(String token){
        String username = JwtUtil.getUsername(token);
        User user=userMapper.findByUsername(username);
        if(user==null){
            throw new RuntimeException("用户不存在");
        }
        return user;
    }
}
