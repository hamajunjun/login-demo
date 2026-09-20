package com.example.logindemo.mapper;

import com.example.logindemo.entity.PostLike;
import com.example.logindemo.dto.PostLikeCountDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostLikeMapper {
    @Insert("INSERT INTO post_like(post_id,user_id) VALUES (#{postId},#{userId})")
    int insert(PostLike postLike);

    @Delete("DELETE FROM post_like WHERE post_id=#{postId} AND user_id=#{userId}")
    int delete(@Param("postId") Long postId,@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM post_like WHERE post_id=#{postId}")
    int countById(@Param("postId") Long postId);

    @Select({
            "<script>",
            "SELECT post_id AS postId, COUNT(*) AS likeCount",
            "FROM post_like",
            "WHERE post_id IN",
            "<foreach collection='postIds' item='postId' open='(' separator=',' close=')'>",
            "#{postId}",
            "</foreach>",
            "GROUP BY post_id",
            "</script>"
    })
    List<PostLikeCountDTO> countByPostIds(@Param("postIds") List<Long> postIds);

    @Select("SELECT * FROM post_like WHERE post_id=#{postId} AND user_id=#{userId}")
    PostLike findPostIdAndUserId(@Param("postId") Long postId,@Param("userId") Long userId);

    @Delete("DELETE FROM post_like WHERE post_id = #{postId}")
    int deleteByPostId(@Param("postId") Long postId);

}
