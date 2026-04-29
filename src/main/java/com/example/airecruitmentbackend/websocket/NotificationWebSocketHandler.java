package com.example.airecruitmentbackend.websocket;

import com.example.airecruitmentbackend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String query = session.getUri().getQuery();
        Long userId = extractUserId(query);
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("WebSocket连接建立: userId={}, sessionId={}", userId, session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        userSessions.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
        log.info("WebSocket连接关闭: sessionId={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 客户端发送的心跳或消息，暂不处理
    }

    public void sendNotification(Long userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送WebSocket消息失败: userId={}", userId, e);
                userSessions.remove(userId);
            }
        }
    }

    public int getActiveConnectionCount() {
        return userSessions.size();
    }

    private Long extractUserId(String query) {
        if (query == null) return null;
        try {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "token".equals(pair[0])) {
                    Claims claims = JwtUtil.parseToken(pair[1]);
                    return claims.get("userId", Long.class);
                }
            }
        } catch (Exception e) {
            log.warn("WebSocket连接token解析失败: {}", e.getMessage());
        }
        return null;
    }
}
