package com.example.logindemo.websocket;

import com.example.logindemo.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知 WebSocket 处理器：管理在线用户，提供"推消息给某个人"的能力
 */
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    // 在线用户登记表：userId -> 这个用户的所有连接（一个用户可能开多个页面，就有多条连接）
    // 外层 Map 用 ConcurrentHashMap，内层 Set 用 newKeySet()，都是线程安全的
    private static final Map<Long, Set<WebSocketSession>> SESSIONS = new ConcurrentHashMap<>();

    // 连接建立时：解析 token 得到 userId，然后登记进"在线表"
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromToken(session);
        if (userId == null) {
            // token 缺失或无效，直接拒绝连接
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        // 把 userId 存进 session，断开时直接从 session 取，不用再解析 token
        session.getAttributes().put("userId", userId);
        // 同一个用户可能有多条连接，用 Set 存起来，别覆盖之前的
        Set<WebSocketSession> sessions = SESSIONS.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
        sessions.add(session);
        System.out.println("用户 " + userId + " 上线，当前在线用户：" + SESSIONS.size());
    }

    // 连接断开时：把用户从"在线表"移除
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            Set<WebSocketSession> sessions = SESSIONS.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                // 这个用户已经没有连接了，就把整个 key 删掉
                if (sessions.isEmpty()) {
                    SESSIONS.remove(userId, sessions);
                }
            }
            System.out.println("用户 " + userId + " 下线，当前在线用户：" + SESSIONS.size());
        }
    }

    // 给指定用户推一条消息（后面 MQ 消费者会调用它）
    public void pushToUser(Long userId, String message) {
        Set<WebSocketSession> sessions = SESSIONS.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        // 该用户可能有多个页面在线，每条连接都推一遍
        for (WebSocketSession session : sessions) {
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    System.err.println("推送给用户 " + userId + " 失败：" + e.getMessage());
                }
            }
        }
    }

    // 从连接地址里解析出 token，再用 JWT 解析出真实 userId，例如 /ws/notification?token=xxx
    private Long getUserIdFromToken(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();  // 拿到 "token=xxx"
            if (query != null) {
                for (String pair : query.split("&")) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2 && "token".equals(kv[0])) {
                        // 信任 JWT 解析结果，不信任前端自己报的 userId
                        return JwtUtil.getUserId(kv[1]);
                    }
                }
            }
        } catch (Exception e) {
            // token 无效或过期时 verifyToken 会抛异常，这里捕获后返回 null
            System.err.println("解析 token 失败：" + e.getMessage());
        }
        return null;
    }
}