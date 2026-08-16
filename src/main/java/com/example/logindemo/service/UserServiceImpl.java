package com.example.logindemo.service;

import com.example.logindemo.entity.User;
import com.example.logindemo.mapper.UserMapper;
import com.example.logindemo.util.PasswordUtil;
import com.example.logindemo.util.RedisUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public User login(String username, String password) {
        // 1. 根据用户名查询用户
        User user = userMapper.findByUsername(username);
        // 2. 用户不存在，直接返回 null
        if (user == null) {
            return null;
        }
        //用户被禁用
        if(user.getStatus()!=null && user.getStatus()==0){
            return null;
        }
        // 3. 校验密码是否正确
        if (PasswordUtil.matches(password, user.getPassword())) {
            return user;
        }
        // 4. 密码错误
        return null;
    }
    @Override
    public boolean register(String username,String password,String email){
        User existUser = userMapper.findByUsername(username);
        if(existUser !=null){
            return false;
        }
        User user=new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encode(password));
        user.setEmail(email);
        user.setRole("USER");
        user.setStatus(1);

        int result = userMapper.insertUser(user);
        return result>0;
    }
    @Override
    public PageInfo<User> listUsers(int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<User> list = userMapper.findAll();
        return new PageInfo<>(list);
    }
    @Override
    public User findByUsername(String username){
        String key="user:info:"+username;
        User user = redisUtil.getObject(key,User.class);
        if(user!=null){
            return user;
        }
        user=userMapper.findByUsername(username);
        if(user!=null){
            redisUtil.setObject(key,user,30, TimeUnit.MINUTES);
        }
        return user;
    }

    @Override
    public User findById(Long id){
        return userMapper.findById(id);
    }

    @Override
    public boolean changePassword(String username,String oldPassword,String newPassword){
        // 1. 根据用户名查询用户
        User user = userMapper.findByUsername(username);
        if(user == null){
            return false;
        }
        // 2. 校验旧密码是否正确
        if(!PasswordUtil.matches(oldPassword,user.getPassword())){
            return false;
        }
        //3. 把新密码加密
        String encodedNewPassword = PasswordUtil.encode(newPassword);

        // 4. 更新到数据库
        int result = userMapper.updatePassword(username,encodedNewPassword);

        // 5. 返回是否更新成功
        return result>0;

    }

    @Override
    public boolean deleteUserById(Long id){
        int result = userMapper.deleteById(id);
        return result>0;
    }

    @Override
    public List<User> searchUsersByUsername(String username){
        return userMapper.findByUsernameLike(username);
    }

    @Override
    public boolean updateUserInfo(Long id, String username, String email){
        User user=new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);

        int result=userMapper.updateUser(user);
        return result>0;
    }

    @Override
    public User updateCurrentUserInfo(String currentUsername,String newUsername,String newEmail){
        User user=userMapper.findByUsername(currentUsername);
        if(user==null){
            return null;
        }
        user.setUsername(newUsername);
        user.setEmail(newEmail);

        int result=userMapper.updateUser(user);
        if(result>0){
            redisUtil.delete("user:info:"+currentUsername);
            return user;
        }
        return null;
    }
    @Override
    public boolean deleteCurrentUser(String username){
        User user=userMapper.findByUsername(username);
        if(user==null){
            return false;
        }
        int result =userMapper.deleteById(user.getId());
        return result>0;
    }

    @Override
    public boolean updateUserStatus(Long id,Integer status){
        int result = userMapper.updateStatus(id,status);
        return result>0;
    }

    @Override
    public boolean resetUserPassword(Long id,String defaultPassword){
        User user=userMapper.findById(id);
        if(user==null){
            return false;
        }
        String encodedPassword =PasswordUtil.encode(defaultPassword);
        int result=userMapper.updatePasswordById(id,encodedPassword);
        return result>0;
    }

    @Override
    public boolean updateAvatar(Long id,String avatar){
        User user=userMapper.findById(id);
        if(user==null){
            return false;
        }
        int result=userMapper.updateAvatar(id,avatar);
        return result>0;
    }
}
