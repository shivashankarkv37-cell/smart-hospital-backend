package com.example.smart_hospital_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DoctorRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String specialization,
        @NotNull @Positive Long departmentId,
        @Positive Long userId) {}
