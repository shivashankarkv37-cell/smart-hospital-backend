package com.example.smart_hospital_backend.dto;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant timestamp, int status, String message, Map<String, String> errors) {}
