package com.example.smart_hospital_backend.dto;
import com.example.smart_hospital_backend.entity.Doctor;
public record DoctorResponse(Long id, String name, String specialization, DepartmentResponse department) {
    public static DoctorResponse from(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialization(),
                DepartmentResponse.from(doctor.getDepartment()));
    }
}
