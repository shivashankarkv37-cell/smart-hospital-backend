package com.example.smart_hospital_backend.dto;

import com.example.smart_hospital_backend.entity.User;
import java.time.Instant;

public record UserResponse(Long id, String fullName, String email, String phone, String role, Instant createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhoneNumber(), user.getRole().name(), user.getCreatedAt());
    }
}
