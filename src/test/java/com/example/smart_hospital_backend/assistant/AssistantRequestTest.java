package com.example.smart_hospital_backend.assistant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

class AssistantRequestTest {
    @Test
    void rejectsBlankMessage() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertFalse(factory.getValidator().validate(new AssistantRequest("  ")).isEmpty());
        }
    }

    @Test
    void acceptsMaximumLengthAndRejectsLongerMessages() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertTrue(validator.validate(new AssistantRequest("x".repeat(1000))).isEmpty());
            assertFalse(validator.validate(new AssistantRequest("x".repeat(1001))).isEmpty());
        }
    }
}
