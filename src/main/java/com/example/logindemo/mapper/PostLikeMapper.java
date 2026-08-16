package com.example.logindemo.mapper;

import com.example.logindemo.entity.PostLike;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PostLikeMapper {
    @Insert("INSERT INTO post_like(post_id,user_id) VALUES (#{postId},#{userId})")
    int insert(PostLike postLike);

    @Delete("DELETE FROM post_like WHERE post_id=#{postId} AND user_id=#{userId}")
    int delete(@Param("postId") Long postId,@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM post_like WHERE post_id=#{postId}")
    int countById(@Param("postId") Long postId);

    @Select("SELECT * FROM post_like WHERE post_id=#{postId} AND user_id=#{userId}")
    PostLike findPostIdAndUserId(@Param("postId") Long postId,@Param("userId") Long userId);
}
