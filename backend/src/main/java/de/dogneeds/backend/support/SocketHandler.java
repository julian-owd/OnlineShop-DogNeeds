package de.dogneeds.backend.support;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;

public class SocketHandler extends TextWebSocketHandler {

    // list of active websocket sessions
    private final ArrayList<WebSocketSession> sessions = new ArrayList<>();

    /**
     * event when a new connection is established
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("Connected " + session.getId());
        sessions.add(session);
    }

    /**
     * event when a new message was received
     */
    @Override
    public void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) throws IOException {
        System.out.println("Received message: " + message.getPayload());
        for (WebSocketSession session : sessions) {
            session.sendMessage(message);
        }
    }

    /**
     * event when a connection is closed
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("Disconnected " + session.getId() + " with code " + status);
        sessions.remove(session);
    }
}
