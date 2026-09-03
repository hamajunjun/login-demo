package com.example.logindemo.mapper;
import com.example.logindemo.entity.Post;

import com.example.logindemo.entity.PostFavorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostFavoriteMapper {

    @Insert("INSERT INTO post_favorite(user_id, post_id) VALUES(#{userId}, #{postId})")
    int insert(PostFavorite postFavorite);

    @Delete("DELETE FROM post_favorite WHERE user_id = #{userId} AND post_id = #{postId}")
    int delete(@Param("userId") Long userId, @Param("postId") Long postId);

    @Select("SELECT * FROM post_favorite WHERE user_id = #{userId} AND post_id = #{postId}")
    PostFavorite findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Select("SELECT p.* FROM post_favorite pf INNER JOIN post p ON p.id=pf.post_id WHERE pf.user_id=#{userId} ORDER BY pf.create_time DESC ")
    List<Post> findFavoritesByUserId(Long userId);

    @Delete("DELETE FROM post_favorite WHERE post_id = #{postId}")
    int deleteByPostId(@Param("postId") Long postId);
}
