package com.example.logindemo.util;


import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    //加密密码
    public static String encode(String password){
        return BCrypt.hashpw(password,BCrypt.gensalt());
    }
    // 校验密码：rawPassword 是用户输入的明文，encodedPassword 是数据库里的密文
    public static boolean matches(String rawPassword,String encodedPassword){
        return BCrypt.checkpw(rawPassword,encodedPassword);
    }
}
