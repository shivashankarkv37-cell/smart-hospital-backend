package com.example.smart_hospital_backend.controller;

import com.example.smart_hospital_backend.dto.AuthenticatedUserResponse;
import com.example.smart_hospital_backend.dto.LoginRequest;
import com.example.smart_hospital_backend.dto.LoginResponse;
import com.example.smart_hospital_backend.dto.RegisterRequest;
import com.example.smart_hospital_backend.dto.UserResponse;
import com.example.smart_hospital_backend.service.AuthService;
import com.example.smart_hospital_backend.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthenticatedUserResponse currentUser(Principal principal) {
        return authService.currentUser(principal.getName());
    }
}
