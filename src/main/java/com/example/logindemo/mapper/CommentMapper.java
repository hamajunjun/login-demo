package com.example.logindemo.mapper;

import com.example.logindemo.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Insert("INSERT INTO comment (post_id, user_id, username, content, parent_id) VALUES (#{postId}, #{userId}, #{username}, #{content}, #{parentId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertComment(Comment comment);

    @Select("SELECT * FROM comment WHERE post_id = #{postId} ORDER BY create_time DESC")
    List<Comment> findByPostId(@Param("postId") Long postId);

    @Select("SELECT * FROM comment WHERE id = #{id}")
    Comment findById(@Param("id") Long id);

    @Delete("DELETE FROM comment WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}