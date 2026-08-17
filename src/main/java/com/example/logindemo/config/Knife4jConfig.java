package com.example.logindemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("登录演示项目接口文档")   // 文档标题
                        .version("1.0")                // 版本号
                        .description("login-demo 项目的接口文档")  // 文档描述
                        .contact(new Contact().name("周佳星"))); // 联系人
    }
}