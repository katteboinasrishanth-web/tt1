package com.restaurant.reservation.service;

import com.restaurant.reservation.exception.ApiException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionManager {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public String createSession(String role, Long id) {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new Session(role, id));
        return token;
    }

    public Session resolve(String token) {
        if (token == null || token.isBlank()) {
            throw new ApiException(401, "Not authenticated");
        }
        Session session = sessions.get(token);
        if (session == null) {
            throw new ApiException(401, "Invalid or expired session, please log in again");
        }
        return session;
    }

    public Session requireRole(String token, String role) {
        Session session = resolve(token);
        if (!session.role().equals(role)) {
            throw new ApiException(403, "Access denied: " + role + " role required");
        }
        return session;
    }

    public void logout(String token) {
        sessions.remove(token);
    }

    public record Session(String role, Long id) {
    }
}