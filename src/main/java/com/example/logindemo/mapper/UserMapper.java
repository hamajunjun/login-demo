package com.example.logindemo.mapper;

import com.example.logindemo.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE username = #{username}")
    public User findByUsername(@Param("username") String username);

    @Insert("INSERT INTO user (username, password, email, role, status,avatar) VALUES (#{username}, #{password}, #{email},#{role}, #{status},#{avatar})")
    public int insertUser(User user);

    @Select("SELECT * FROM user")
    public List<User> findAll();

    @Select("SELECT * FROM user WHERE id=#{id}")
    public User findById(@Param("id") Long id);

    @Update("UPDATE user SET password = #{password} WHERE username = #{username}")
    public int updatePassword(@Param("username") String username,@Param("password") String password);

    @Delete("DELETE FROM user WHERE id=#{id}")
    public int deleteById(@Param("id")Long id);

    @Select("SELECT * FROM user WHERE username LIKE CONCAT('%',#{username},'%')")
    public List<User> findByUsernameLike(@Param("username") String username);

    @Update("UPDATE user SET username=#{username},email=#{email} WHERE ID=#{id}")
    public int updateUser(User user);

    @Update("UPDATE user SET status = #{status} WHERE id=#{id}")
    public int updateStatus(@Param("id") Long id,@Param("status") Integer status);

    @Update("UPDATE user SET password = #{password} WHERE id=#{id}")
    public int updatePasswordById(@Param("id") Long id,@Param("password") String password);

    @Update("UPDATE user SET avatar=#{avatar} WHERE id=#{id}")
    public int updateAvatar(@Param("id") Long id,@Param("avatar") String avatar);
}






















