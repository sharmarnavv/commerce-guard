package com.commerceguard.pipeline.websocket;

import com.commerceguard.pipeline.model.MonitoringData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DashboardWebSocketHandler implements WebSocketHandler {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("New WebSocket connection established: {}", session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        // Handle incoming messages if needed
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        sessions.remove(session.getId());
        log.info("WebSocket connection closed: {}", session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void broadcastUpdate(MonitoringData data) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(data);
        } catch (IOException e) {
            log.error("Error serializing monitoring data", e);
            return;
        }

        TextMessage message = new TextMessage(payload);
        sessions.values().parallelStream().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            } catch (IOException e) {
                log.error("Error sending WebSocket message", e);
            }
        });
    }
}
