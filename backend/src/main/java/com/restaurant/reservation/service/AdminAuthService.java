package com.restaurant.reservation.service;

import com.restaurant.reservation.dto.AdminLoginRequest;
import com.restaurant.reservation.exception.ApiException;
import com.restaurant.reservation.model.Admin;
import com.restaurant.reservation.repository.AdminRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final SessionManager sessionManager;

    public AdminAuthService(AdminRepository adminRepository, SessionManager sessionManager) {
        this.adminRepository = adminRepository;
        this.sessionManager = sessionManager;
    }

    public Map<String, Object> login(AdminLoginRequest req) {
        Admin admin = adminRepository.findByUsername(req.getUsername().trim())
                .orElseThrow(() -> new ApiException(401, "Invalid admin credentials"));
        if (!admin.getPassword().equals(req.getPassword())) {
            throw new ApiException(401, "Invalid admin credentials");
        }
        String token = sessionManager.createSession("ADMIN", admin.getAdminId());
        return Map.of("token", token, "role", "ADMIN", "username", admin.getUsername());
    }

    public void logout(String token) {
        sessionManager.logout(token);
    }
}