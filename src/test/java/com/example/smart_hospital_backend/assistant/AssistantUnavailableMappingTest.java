package com.example.smart_hospital_backend.assistant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.smart_hospital_backend.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;

class AssistantUnavailableMappingTest {
    @Test
    void disabledAssistantMapsToControlledServiceUnavailableResponse() {
        var error = new GlobalExceptionHandler().assistantUnavailable(new AssistantUnavailableException());

        assertEquals(503, error.status());
        assertEquals("Hospital FAQ assistant is unavailable", error.message());
    }
}
