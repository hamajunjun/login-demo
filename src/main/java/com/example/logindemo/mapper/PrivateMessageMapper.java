package com.example.logindemo.mapper;
import com.example.logindemo.entity.PrivateMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PrivateMessageMapper {

    @Insert("INSERT INTO private_message(sender_id, receiver_id, content) VALUES(#{senderId}, #{receiverId},#{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PrivateMessage message);

    @Select("SELECT m.* ,u.username AS sender_username FROM private_message m INNER JOIN " +
            "user u ON m.sender_id = u.id WHERE m.receiver_id=" +
            "#{receiverId} ORDER BY m.create_time DESC")
    List<PrivateMessage> findInboxByReceiverId(Long receiverId);

    @Select("SELECT m.*, u.username AS receiver_username FROM private_message m INNER JOIN " +
            "user u ON m.receiver_id = u.id WHERE m.sender_id = #{senderId} ORDER BY m.create_time DESC")
    List<PrivateMessage> findOutboxBySenderId(Long senderId);

    @Select("SELECT COUNT(*) FROM private_message WHERE receiver_id = #{receiverId} AND is_read=0")
    int countUnread(Long receiverId);

    @Update("UPDATE private_message SET is_read = 1 WHERE id = #{id} AND receiver_id = #{receiverId}")
    int markRead(@Param("id") Long id, @Param("receiverId") Long receiverId);
}
