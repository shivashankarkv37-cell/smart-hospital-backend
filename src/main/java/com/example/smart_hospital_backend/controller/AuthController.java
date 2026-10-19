package com.example.smart_hospital_backend.controller;

import com.example.smart_hospital_backend.dto.RegisterRequest;
import com.example.smart_hospital_backend.dto.LoginResponse;
import com.example.smart_hospital_backend.dto.UserResponse;
import com.example.smart_hospital_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) { this.userService = userService; }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@AuthenticationPrincipal UserDetails authenticatedUser) {
        String role = authenticatedUser.getAuthorities().stream()
                .findFirst().map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .orElse("PATIENT");
        return new LoginResponse(authenticatedUser.getUsername(), role, "Authentication successful");
    }
}
