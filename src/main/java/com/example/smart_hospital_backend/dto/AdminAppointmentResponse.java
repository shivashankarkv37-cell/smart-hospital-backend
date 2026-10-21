package com.example.smart_hospital_backend.dto;

import com.example.smart_hospital_backend.entity.Appointment;
import com.example.smart_hospital_backend.entity.AppointmentStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record AdminAppointmentResponse(
        Long id,
        PatientSummary patient,
        DoctorSummary doctor,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,
        String reason,
        Instant createdAt) {

    public static AdminAppointmentResponse from(Appointment appointment) {
        var patient = appointment.getPatient();
        var doctor = appointment.getDoctor();
        return new AdminAppointmentResponse(appointment.getId(),
                new PatientSummary(patient.getId(), patient.getName(), patient.getEmail()),
                new DoctorSummary(doctor.getId(), doctor.getName(), doctor.getSpecialization(),
                        doctor.getDepartment().getName()),
                appointment.getAppointmentDate(), appointment.getStartTime(), appointment.getEndTime(),
                appointment.getStatus(), appointment.getReason(), appointment.getCreatedAt());
    }

    public record PatientSummary(Long id, String name, String email) {}
    public record DoctorSummary(Long id, String name, String specialization, String departmentName) {}
}
