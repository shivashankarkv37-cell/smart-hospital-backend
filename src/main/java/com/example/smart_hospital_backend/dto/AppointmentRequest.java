package com.example.smart_hospital_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentRequest(
        @NotNull(message = "Patient ID is required") @Positive(message = "Patient ID must be positive")
                Long patientId,
        @NotNull(message = "Doctor ID is required") @Positive(message = "Doctor ID must be positive")
                Long doctorId,
        @NotNull(message = "Appointment date is required") LocalDate appointmentDate,
        @NotNull(message = "Start time is required") LocalTime startTime,
        @NotNull(message = "End time is required") LocalTime endTime,
        @NotBlank(message = "Reason is required") @Size(max = 500, message = "Reason must not exceed 500 characters")
                String reason) {}
