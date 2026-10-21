package com.example.smart_hospital_backend.dto;
import com.example.smart_hospital_backend.entity.Patient;
public record PatientResponse(Long id, String name, String email, String phone) {
    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(), patient.getName(), patient.getEmail(), patient.getPhone());
    }
}
