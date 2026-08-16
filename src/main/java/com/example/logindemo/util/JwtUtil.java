package com.example.logindemo.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.logindemo.entity.User;

import java.util.Date;

public class JwtUtil {

    // 密钥，用来签名和验签。实际项目建议放到配置文件中，不要直接写死在代码里
    private static final String SECRET = "your-secret-key";

    // Token 过期时间：7 天，单位毫秒
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000;

    /**
     * 生成 JWT Token
     */
    public static String generateToken(User user) {
        // 计算过期时间
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRE_TIME);

        // 创建并签名 token
        return JWT.create()
                .withClaim("userId", user.getId())           // 放入用户 ID
                .withClaim("username", user.getUsername())   // 放入用户名
                .withClaim("role",user.getRole())
                .withExpiresAt(expireDate)                   // 设置过期时间
                .sign(Algorithm.HMAC256(SECRET));            // 用 HMAC256 算法签名
    }

    /**
     * 验证 Token 是否合法
     * 合法返回 DecodedJWT，不合法会抛出 JWTVerificationException
     */
    public static DecodedJWT verifyToken(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
        return verifier.verify(token);
    }

    /**
     * 从 Token 中获取用户名
     */
    public static String getUsername(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("username").asString();
    }

    /**
     * 从 Token 中获取用户角色
     */
    public static String getRole(String token){
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("role").asString();
    }

}