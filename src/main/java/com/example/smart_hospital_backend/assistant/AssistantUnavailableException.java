package com.example.smart_hospital_backend.assistant;

public class AssistantUnavailableException extends RuntimeException {
    public AssistantUnavailableException() {
        super("Hospital FAQ assistant is disabled");
    }
}
