package com.example.smart_hospital_backend.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.smart_hospital_backend.dto.PatientRegistrationRequest;
import com.example.smart_hospital_backend.entity.Patient;
import com.example.smart_hospital_backend.repository.PatientRepository;
import com.example.smart_hospital_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PatientServiceRegistrationTest {
    @Test
    void registrationStoresBcryptHashAndReturnsNoPassword() {
        PatientRepository patients = mock(PatientRepository.class);
        UserRepository users = mock(UserRepository.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        when(patients.saveAndFlush(any(Patient.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(users.saveAndFlush(any(com.example.smart_hospital_backend.entity.User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        PatientService service = new PatientService(patients, users, passwordEncoder);

        var response = service.register(new PatientRegistrationRequest(
                "Sam Patient", "sam@example.com", "9876543210", "StrongPassword123"));

        var saved = org.mockito.ArgumentCaptor.forClass(Patient.class);
        org.mockito.Mockito.verify(patients).saveAndFlush(saved.capture());
        assertTrue(saved.getValue().getPasswordHash().startsWith("$2"));
        assertTrue(passwordEncoder.matches("StrongPassword123", saved.getValue().getPasswordHash()));
        assertFalse(response.toString().contains("password"));
        assertFalse(java.util.Arrays.stream(response.getClass().getRecordComponents())
                .anyMatch(component -> component.getName().toLowerCase().contains("password")));
    }
}
