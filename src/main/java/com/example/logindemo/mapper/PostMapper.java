package com.example.logindemo.mapper;

import com.example.logindemo.entity.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostMapper {

    @Insert("INSERT INTO post (title,content,user_id,username,community_id,rating,view_count) VALUES (#{title}," +
            "#{content},#{userId},#{username},#{communityId},#{rating},#{viewCount})")
    @Options(useGeneratedKeys=true,keyProperty="id")
    int insertPost(Post post);

    @Select("SELECT * FROM post ORDER BY create_time DESC")
    List<Post> findAll();

    @Select("SELECT * FROM post WHERE id=#{id}")
    Post findById(Long id);

    @Update("UPDATE post SET title=#{title},content=#{content},update_time=NOW() WHERE id=#{id}")
    int updatePost(@Param("id") Long id,
                   @Param("title") String title,
                   @Param("content") String content);

    @Delete("DELETE FROM post WHERE id=#{id}")
    int deletePost(@Param("id") Long id);

    @Select("SELECT * FROM post WHERE community_id=#{communityId} ORDER BY create_time DESC")
    List<Post> findByCommunityId(Long communityId);

    @Select("SELECT * FROM post WHERE user_id=#{userId} ORDER BY create_time DESC")
    List<Post> findByUserId(Long userId);

    @Select("SELECT * FROM post WHERE title LIKE CONCAT('%',#{keyword},'%') OR content LIKE CONCAT('%',#{keyword},'%') ORDER BY create_time DESC")
    List<Post> findByKeyword(@Param("keyword") String keyWord);

    @Update("UPDATE post SET view_count=view_count+1 WHERE id=#{id}")
    int increaseViewCount(Long id);

    @Select("SELECT * FROM post ORDER BY view_count DESC,create_time DESC")
    List<Post> findHotList();

}
