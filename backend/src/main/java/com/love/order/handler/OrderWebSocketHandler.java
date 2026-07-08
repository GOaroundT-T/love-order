package com.love.order.handler;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单 WebSocket 处理器 — 情侣实时互动
 */
@Slf4j
@Component
public class OrderWebSocketHandler extends TextWebSocketHandler {

    /** userId → session */
    private static final Map<Long, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserId(session);
        if (userId != null) {
            SESSION_MAP.put(userId, session);
            log.info("用户 {} 建立 WebSocket 连接", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 心跳等简单处理
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserId(session);
        if (userId != null) {
            SESSION_MAP.remove(userId);
            log.info("用户 {} 断开 WebSocket 连接", userId);
        }
    }

    /**
     * 向指定用户推送消息
     */
    public void sendToUser(Long userId, String msg) {
        WebSocketSession session = SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(msg));
            } catch (Exception e) {
                log.error("WebSocket 推送失败 userId={}", userId, e);
            }
        }
    }

    private Long getUserId(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        if (query != null && query.contains("userId=")) {
            try {
                return Long.parseLong(query.substring(query.indexOf("userId=") + 7).split("&")[0]);
            } catch (Exception ignored) {}
        }
        return null;
    }
}
