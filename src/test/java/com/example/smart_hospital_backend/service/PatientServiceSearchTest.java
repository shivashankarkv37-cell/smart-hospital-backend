package com.example.smart_hospital_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.smart_hospital_backend.entity.Patient;
import com.example.smart_hospital_backend.repository.PatientRepository;
import com.example.smart_hospital_backend.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

class PatientServiceSearchTest {
    private final PatientRepository patients = mock(PatientRepository.class);
    private final PatientService service = new PatientService(
            patients, mock(UserRepository.class), mock(PasswordEncoder.class));

    @Test
    void receptionistSearchMapsOnlySafePatientResponseFields() {
        Patient patient = new Patient("Sam Patient", "sam@example.com", "9876543210", "secret-hash");
        when(patients.searchForReception(org.mockito.ArgumentMatchers.eq(null),
                org.mockito.ArgumentMatchers.eq("sam"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(patient)));

        var result = service.searchForReception(" sam ", PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals("sam@example.com", result.getContent().getFirst().email());
        assertEquals("Sam Patient", result.getContent().getFirst().name());
    }
}
