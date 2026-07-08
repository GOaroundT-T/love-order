package com.love.order.handler;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单 WebSocket 处理器 — 情侣实时互动
 */
@Slf4j
@Component
public class OrderWebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = resolveUserId(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("请先登录"));
            return;
        }
        session.getAttributes().put("userId", userId);
        SESSION_MAP.put(userId, session);
        log.info("用户 {} 建立 WebSocket 连接", userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 心跳等简单处理，前端发送 ping 时返回 pong。
        if ("ping".equalsIgnoreCase(message.getPayload())) {
            try {
                session.sendMessage(new TextMessage("pong"));
            } catch (Exception e) {
                log.warn("WebSocket 心跳响应失败", e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getSessionUserId(session);
        if (userId != null) {
            SESSION_MAP.remove(userId);
            log.info("用户 {} 断开 WebSocket 连接", userId);
        }
    }

    /** 向指定用户推送消息 */
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

    private Long getSessionUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId instanceof Long ? (Long) userId : null;
    }

    private Long resolveUserId(WebSocketSession session) {
        String token = getQueryParam(session, "token");
        if (token == null || token.isBlank()) token = getQueryParam(session, "satoken");
        if (token == null || token.isBlank()) return null;

        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) return null;
            if (loginId instanceof Number number) return number.longValue();
            return Long.parseLong(String.valueOf(loginId));
        } catch (Exception e) {
            log.warn("WebSocket token 校验失败");
            return null;
        }
    }

    private String getQueryParam(WebSocketSession session, String name) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query == null || query.isBlank()) return null;
        return Arrays.stream(query.split("&"))
                .map(item -> item.split("=", 2))
                .filter(pair -> pair.length == 2 && name.equals(pair[0]))
                .map(pair -> pair[1])
                .findFirst()
                .orElse(null);
    }
}
