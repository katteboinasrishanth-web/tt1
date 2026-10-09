package com.restaurant.reservation.dto;

import com.restaurant.reservation.model.User;

public class UserResponse {

    private Long userId;
    private String name;
    private String email;
    private String phone;

    public static UserResponse from(User u) {
        UserResponse dto = new UserResponse();
        dto.userId = u.getUserId();
        dto.name = u.getName();
        dto.email = u.getEmail();
        dto.phone = u.getPhone();
        return dto;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}