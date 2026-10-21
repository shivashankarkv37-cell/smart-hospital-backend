package com.example.smart_hospital_backend.assistant;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RuleBasedFaqAssistantTest {
    private final RuleBasedFaqAssistant assistant = new RuleBasedFaqAssistant();

    @Test
    void recognizesAppointmentBookingQuestion() {
        assertTrue(assistant.answer("How do I BOOK an appointment?").contains("POST /api/appointments"));
    }

    @Test
    void recognizesCancellationQuestion() {
        assertTrue(assistant.answer("How do I cancel my appointment?").contains("does not change appointment records"));
    }

    @Test
    void recognizesDoctorDirectoryQuestion() {
        assertTrue(assistant.answer("Where can I find physicians?").contains("GET /api/doctors"));
    }

    @Test
    void recognizesQueueTokenQuestion() {
        assertTrue(assistant.answer("How do queue tokens work?").contains("queue token"));
    }

    @Test
    void unsupportedQuestionsReceiveSafeFallback() {
        assertTrue(assistant.answer("What are today's opening hours?").contains("contact hospital staff"));
    }

    @Test
    void medicalAdviceRequestReceivesSafetyResponse() {
        String answer = assistant.answer("Can you diagnose my symptoms?");
        assertTrue(answer.contains("cannot diagnose"));
        assertTrue(answer.contains("qualified healthcare professional"));
    }

    @Test
    void emergencyRequestReceivesEmergencyDirection() {
        assertTrue(assistant.answer("I have severe chest pain").contains("local emergency services"));
    }
}
