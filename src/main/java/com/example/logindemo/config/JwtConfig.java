package com.example.logindemo.config;

import com.example.logindemo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @PostConstruct
    public void init() {
        JwtUtil.setSecret(secret);
    }
}
