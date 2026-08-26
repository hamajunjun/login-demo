package com.example.logindemo.service;

import com.example.logindemo.entity.User;
import com.example.logindemo.mapper.UserMapper;
import com.example.logindemo.util.BloomFilterUtil;
import com.example.logindemo.util.JwtUtil;
import com.example.logindemo.util.PasswordUtil;
import com.example.logindemo.util.RedisUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
    @Autowired
    private BloomFilterUtil bloomFilterUtil;

    @Autowired
    private RedissonClient redissonClient;

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
    public boolean register(String username, String password, String email) {
        // 1. 先用布隆过滤器判断用户名是否可能存在
        if (bloomFilterUtil.mightContain(username)) {
            // 2. 可能存在，需要进一步查数据库确认（因为有误判）
            User existUser = userMapper.findByUsername(username);
            if (existUser != null) {
                return false;  // 用户名真的已存在
            }
        }

        // 3. 用户名肯定不存在，可以注册
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encode(password));
        user.setEmail(email);
        user.setRole("USER");
        user.setStatus(1);

        // 4. 插入数据库
        int result = userMapper.insertUser(user);
        if (result <= 0) {
            return false;
        }

        // 5. 注册成功后，把用户名加入布隆过滤器
        bloomFilterUtil.add(username);

        return true;
    }
    @Override
    public PageInfo<User> listUsers(int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<User> list = userMapper.findAll();
        return new PageInfo<>(list);
    }
    @Override
    public User findByUsername(String username) {
        // 1. 先用布隆过滤器判断
        // 如果布隆过滤器说肯定不存在，直接返回 null
        if (!bloomFilterUtil.mightContain(username)) {
            return null;
        }

        // 2. 原来的缓存逻辑
        String key = "user:info:" + username;

        User user = redisUtil.getObject(key, User.class);
        if (user != null) {
            return user;
        }

        // 3. 缓存没有，加分布式锁
        RLock lock = redissonClient.getLock("lock:" + key);
        lock.lock();
        try {
            // 拿到锁后再查一次缓存
            user = redisUtil.getObject(key, User.class);
            if (user != null) {
                return user;
            }

            // 4. 查数据库
            user = userMapper.findByUsername(username);

            // 5. 数据库也没有，缓存空值
            if (user == null) {
                redisUtil.setObject(key, null, 5, TimeUnit.MINUTES);
                return null;
            }

            // 6. 写入缓存
            redisUtil.setObject(key, user, 30, TimeUnit.MINUTES);
            return user;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public User getCurrentUser(String token) {
        String username = JwtUtil.getUsername(token);
        User user = findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
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
            throw new RuntimeException("用户不存在");
        }
        // 2. 校验旧密码是否正确
        if(!PasswordUtil.matches(oldPassword,user.getPassword())){
            throw new RuntimeException("旧密码错误");
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
    public boolean updateUserInfo(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);

        int result = userMapper.updateUser(user);
        if (result > 0) {
            // 用户名变了，加入布隆过滤器
            bloomFilterUtil.add(username);
            return true;
        }
        return false;
    }

    @Override
    public User updateCurrentUserInfo(String currentUsername, String newUsername, String newEmail) {
        User user = userMapper.findByUsername(currentUsername);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setUsername(newUsername);
        user.setEmail(newEmail);

        int result = userMapper.updateUser(user);
        if (result > 0) {
            // 删除旧缓存
            redisUtil.delete("user:info:" + currentUsername);

            // 新用户名加入布隆过滤器
            bloomFilterUtil.add(newUsername);

            return user;
        }
        throw new RuntimeException("修改失败");
    }
    @Override
    public boolean deleteCurrentUser(String username){
        User user=userMapper.findByUsername(username);
        if(user==null){
            throw new RuntimeException("用户不存在");
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
            throw new RuntimeException("用户不存在");
        }
        int result=userMapper.updateAvatar(id,avatar);
        return result>0;
    }
}
