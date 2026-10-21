package com.example.smart_hospital_backend.dto;

import com.example.smart_hospital_backend.entity.User;
import com.example.smart_hospital_backend.entity.UserRole;

public record AuthenticatedUserResponse(Long id, String email, UserRole role) {
    public static AuthenticatedUserResponse from(User user) {
        return new AuthenticatedUserResponse(user.getId(), user.getEmail(), user.getRole());
    }
}
