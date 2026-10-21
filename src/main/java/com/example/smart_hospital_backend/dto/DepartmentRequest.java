package com.example.smart_hospital_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank(message = "Department name must not be blank")
        @Size(max = 100, message = "Department name must not exceed 100 characters")
        String name,
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description) {}
