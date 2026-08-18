package com.example.logindemo.mapper;

import com.example.logindemo.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Select("SELECT * FROM notification WHERE user_id=#{userId} ORDER BY create_time DESC")
    List<Notification> findByUserId(Long userId);
}
