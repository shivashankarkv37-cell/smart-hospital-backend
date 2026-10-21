package com.example.smart_hospital_backend.dto;

import com.example.smart_hospital_backend.entity.Appointment;
import com.example.smart_hospital_backend.entity.AppointmentStatus;
import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,
        String reason,
        DoctorDetails doctor) {

    public static AppointmentResponse from(Appointment appointment) {
        var doctor = appointment.getDoctor();
        var department = doctor.getDepartment();
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getReason(),
                new DoctorDetails(
                        doctor.getId(),
                        doctor.getName(),
                        doctor.getSpecialization(),
                        new DepartmentResponse(
                                department.getId(), department.getName(), department.getDescription())));
    }

    public record DoctorDetails(
            Long id, String name, String specialization, DepartmentResponse department) {}
}
