package com.example.logindemo.mapper;

import com.example.logindemo.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Select("SELECT * FROM notification WHERE user_id=#{userId} ORDER BY create_time DESC")
    List<Notification> findByUserId(Long userId);

    @Insert("INSERT INTO notification(user_id, type, content, is_read) " +
            "VALUES(#{userId}, #{type}, #{content}, 0)")
    int insert(Notification notification);

    @Update("UPDATE notification SET is_read=1 WHERE id=#{id} AND user_id=#{userId}")
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id=#{userId} AND is_read=0")
    int countUnread(Long userId);

    @Update("UPDATE notification SET is_read=1 WHERE is_read=0 AND user_id=#{userId}")
    int markAllRead(Long userId);

    @Select("SELECT * FROM notification WHERE user_id=#{userId} AND tpye=#{type} ORDER BY create_time DESC")
    List<Notification> findByUserIdAndType(@Param("userId") Long userId,@Param("type") String type);
}
