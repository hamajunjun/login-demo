package com.example.logindemo.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知 WebSocket 处理器：管理在线用户，提供"推消息给某个人"的能力
 */
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    // 在线用户登记表：userId -> 对应的连接会话
    // 用 ConcurrentHashMap 是因为会有多个线程同时读写（连接线程 + MQ 消费者线程）
    private static final Map<Long, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    // 连接建立时：把用户登记进"在线表"
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            SESSIONS.put(userId, session);
            System.out.println("用户 " + userId + " 上线，当前在线：" + SESSIONS.size());
        }
    }

    // 连接断开时：把用户从"在线表"移除
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            SESSIONS.remove(userId);
            System.out.println("用户 " + userId + " 下线，当前在线：" + SESSIONS.size());
        }
    }

    // 给指定用户推一条消息（后面 MQ 消费者会调用它）
    public void pushToUser(Long userId, String message) {
        WebSocketSession session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                System.err.println("推送给用户 " + userId + " 失败：" + e.getMessage());
            }
        }
    }

    // 从连接地址里解析出 userId，例如 /ws/notification?userId=1
    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();  // 拿到 "userId=1"
            if (query != null) {
                for (String pair : query.split("&")) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2 && "userId".equals(kv[0])) {
                        return Long.parseLong(kv[1]);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析 userId 失败：" + e.getMessage());
        }
        return null;
    }
}