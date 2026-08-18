package com.example.logindemo.mapper;

import com.example.logindemo.entity.User;
import com.example.logindemo.entity.UserFollow;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserFollowMapper {

    @Insert("INSERT INTO user_follow(follower_id,following_id) VALUES(#{followerId},#{followingId})")
    @Options(useGeneratedKeys = true,keyProperty="id")
    int insert(UserFollow userFollow);

    @Delete("DELETE FROM user_follow WHERE follower_id=#{followerId} AND following_id=#{followingId}")
    int delete(@Param("followerId") Long followerId,@Param("followingId") Long followingId);

    @Select("SELECT * FROM user_follow WHERE follower_id=#{followerId} ORDER BY create_time DESC")
    List<UserFollow> findByFollowerId(Long followerId);

    @Select("SELECT * FROM user_follow WHERE following_id=#{followingId} ORDER BY create_time DESC")
    List<UserFollow> findByFollowingId(Long followingId);

    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id=#{followerId}")
    int countFollowing(Long followerId);

    @Select("SELECT COUNT(*) FROM user_follow WHERE following_id=#{followingId}")
    int countFollowers(Long followingId);

    @Select("SELECT * FROM user_follow WHERE follower_id=#{followerId} AND following_id=#{followingId} LIMIT 1")
    UserFollow findOne(@Param("followerId") Long followerId,@Param("followingId") Long followingId);

    /**
     * 查询我关注的人（带用户信息，JOIN 查询）
     */
    @Select("SELECT u.* FROM user u INNER JOIN user_follow f ON u.id = f.following_id WHERE f.follower_id=#{followerId} ORDER BY f.create_time DESC")
    List<User> findFollowingUsers(Long followerId);

    /**
     * 查询关注我的人（带用户信息，JOIN 查询）
     */
    @Select("SELECT u.* FROM user u INNER JOIN user_follow f ON u.id = f.follower_id WHERE f.following_id=#{followingId} ORDER BY f.create_time DESC")
    List<User> findFollowerUsers(Long followingId);
}
