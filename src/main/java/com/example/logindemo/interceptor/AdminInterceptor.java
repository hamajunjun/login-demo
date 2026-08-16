package com.example.logindemo.interceptor;

import com.example.logindemo.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
            Exception {
        // 1. 从请求头中获取 token
        String token = request.getHeader("Authorization");

        // 2. 如果没有 token，返回 401
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token is missing");
            return false;
        }

        // 3. 验证 token 是否合法
        try {
            JwtUtil.verifyToken(token);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token is invalid");
            return false;
        }

        // 4. 从 token 中获取角色
        String role = JwtUtil.getRole(token);

        // 5. 判断是否是管理员
        if (!"ADMIN".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Forbidden: admin only");
            return false;
        }

        // 6. 是管理员，放行
        return true;
    }
}