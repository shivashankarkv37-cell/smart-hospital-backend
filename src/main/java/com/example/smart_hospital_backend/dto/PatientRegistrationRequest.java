package com.example.smart_hospital_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PatientRegistrationRequest(
        @NotBlank(message = "Name must not be blank")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        @Pattern(regexp = "^[\\p{L}][\\p{L} .'-]*$", message = "Name contains invalid characters")
        String name,

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Enter a valid email address")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email,

        @NotBlank(message = "Phone number must not be blank")
        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Enter a valid phone number")
        String phone,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password) {}
