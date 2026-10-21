package com.example.smart_hospital_backend.assistant;

import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class RuleBasedFaqAssistant {
    private static final String UNSUPPORTED =
            "I can help with general app navigation, registration, appointments, doctors, departments, and queue tokens. "
                    + "For hospital-specific policies or information not covered here, please contact hospital staff.";
    private static final String MEDICAL_ADVICE =
            "I cannot diagnose symptoms, prescribe medication, or recommend individual treatment. "
                    + "Please contact a qualified healthcare professional for medical advice.";
    private static final String EMERGENCY =
            "If you may be experiencing a medical emergency, contact local emergency services now or go to the nearest emergency department.";

    public String answer(String message) {
        String normalized = normalize(message);
        if (isEmergency(normalized)) {
            return EMERGENCY;
        }
        if (isMedicalAdviceRequest(normalized)) {
            return MEDICAL_ADVICE;
        }
        if (hasAny(normalized, "register", "registration", "sign up", "signup", "create account", "new account")) {
            return "To create a patient login, use POST /api/auth/register with your name, email, phone, and password. "
                    + "The separate patient profile is created through POST /api/patients. The assistant cannot create accounts for you.";
        }
        if (hasAny(normalized, "log in", "login", "sign in", "signin", "password reset", "forgot password")) {
            return "Use the login form, which sends your registered email and password to POST /api/auth/login. "
                    + "A successful login returns a bearer access token. Contact hospital staff if you need help recovering access.";
        }
        if (hasAny(normalized, "cancel", "cancellation", "cancelling", "canceling", "reschedule")) {
            return "To cancel, open your appointment and use its cancel action (PATCH /api/appointments/{appointmentId}/cancel). "
                    + "Patients can cancel their own booked appointments; administrators can cancel appointments. "
                    + "This assistant does not change appointment records.";
        }
        if ((mentionsAppointment(normalized) && hasAny(normalized,
                        "book", "booking", "schedule", "scheduling", "reserve", "make"))
                || hasAny(normalized, "appointment booking", "appointments")) {
            return "Sign in as a patient, browse the doctor directory, and submit the appointment form. "
                    + "The booking API is POST /api/appointments and checks ownership, time validity, and schedule conflicts. "
                    + "This assistant does not book or confirm appointments.";
        }
        if (hasAny(normalized, "doctor", "physician", "specialist")) {
            return "Browse the doctor directory with GET /api/doctors. Doctor schedules and availability are not provided by this FAQ assistant.";
        }
        if (hasAny(normalized, "department", "departments", "specialty", "specialities", "specialties")) {
            return "Browse hospital departments with GET /api/departments. For details not shown there, please contact hospital staff.";
        }
        if (hasAny(normalized, "queue", "token", "check in", "checkin", "waiting number")) {
            return "After an appointment, authorized reception staff can check the patient in; the system then creates a queue token. "
                    + "Patients can check their own queue status with GET /api/queue/my-status. The assistant cannot check anyone in or view queue records.";
        }
        if (hasAny(normalized, "navigate", "navigation", "where do i", "how do i use", "which page", "app sections")) {
            return "The backend supports registration and login, doctor and department directories, appointments, and queue status. "
                    + "Use the corresponding section in your application; exact screen names depend on the frontend.";
        }
        return UNSUPPORTED;
    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }
        String noAccents = Normalizer.normalize(input, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}+", "");
        return noAccents.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private boolean isEmergency(String message) {
        return hasAny(message, "emergency", "cannot breathe", "cant breathe", "trouble breathing",
                "can t breathe", "shortness of breath", "chest pain", "unconscious",
                "bleeding", "overdose", "suicidal");
    }

    private boolean isMedicalAdviceRequest(String message) {
        return hasAny(message, "diagnose", "diagnosis", "prescribe", "prescription", "dosage", "dose",
                "what disease", "what illness", "what is wrong with me", "recommend medicine",
                "recommend medication", "what medicine should i take", "what medication should i take",
                "should i take", "can i take", "which medicine", "which medication",
                "medicine for my", "medication for my", "symptoms mean", "symptom mean",
                "treatment", "symptom", "symptoms",
                "treat my symptoms", "interpret my symptoms");
    }

    private boolean mentionsAppointment(String message) {
        return hasAny(message, "appointment", "visit");
    }

    private boolean hasAny(String message, String... phrases) {
        for (String phrase : phrases) {
            if (message.contains(phrase)) {
                return true;
            }
        }
        return false;
    }
}
