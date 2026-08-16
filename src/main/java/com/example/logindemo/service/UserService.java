package com.example.logindemo.service;

import com.example.logindemo.entity.User;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface UserService {

    User login(String username, String password);

    boolean register(String username,String password,String email);

    PageInfo<User> listUsers(int pageNum, int pageSize);

    User findByUsername(String username);

    User findById(Long id);

    boolean changePassword(String username,String oldPassword,String newPassword);

    boolean deleteUserById(Long id);

    List<User> searchUsersByUsername(String username);

    boolean updateUserInfo(Long id,String username,String email);

    User updateCurrentUserInfo(String currentUsername,String newUsername,String newEmail);

    boolean deleteCurrentUser(String username);

    boolean updateUserStatus(Long id,Integer status);

    boolean resetUserPassword(Long id,String defaultPassword);

    boolean updateAvatar(Long id,String avatar);
}
























