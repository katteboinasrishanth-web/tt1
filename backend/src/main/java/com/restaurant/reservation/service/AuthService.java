package com.restaurant.reservation.service;

import com.restaurant.reservation.dto.LoginRequest;
import com.restaurant.reservation.dto.RegisterRequest;
import com.restaurant.reservation.dto.UserResponse;
import com.restaurant.reservation.exception.ApiException;
import com.restaurant.reservation.model.User;
import com.restaurant.reservation.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public AuthService(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    public Map<String, Object> register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ApiException(409, "An account with this email already exists");
        }
        User user = new User(req.getName(), req.getEmail().trim().toLowerCase(),
                req.getPassword(), req.getPhone());
        userRepository.save(user);
        String token = sessionManager.createSession("CUSTOMER", user.getUserId());
        return Map.of("token", token, "role", "CUSTOMER", "user", UserResponse.from(user));
    }

    public Map<String, Object> login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(401, "Invalid email or password"));
        if (!user.getPassword().equals(req.getPassword())) {
            throw new ApiException(401, "Invalid email or password");
        }
        String token = sessionManager.createSession("CUSTOMER", user.getUserId());
        return Map.of("token", token, "role", "CUSTOMER", "user", UserResponse.from(user));
    }

    public void logout(String token) {
        sessionManager.logout(token);
    }
}