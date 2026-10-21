package com.example.smart_hospital_backend.dto;

import com.example.smart_hospital_backend.entity.QueueEntry;
import com.example.smart_hospital_backend.entity.QueueStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record QueueEntryResponse(
        Long id,
        String tokenNumber,
        Long appointmentId,
        String patientName,
        Long doctorId,
        String doctorName,
        String departmentName,
        LocalDate queueDate,
        QueueStatus status,
        Integer queuePosition,
        LocalDateTime checkedInAt,
        LocalDateTime consultationStartedAt,
        LocalDateTime consultationCompletedAt) {

    public static QueueEntryResponse from(QueueEntry entry, Integer position) {
        var appointment = entry.getAppointment();
        var doctor = appointment.getDoctor();
        return new QueueEntryResponse(entry.getId(), entry.getTokenNumber(), appointment.getId(),
                appointment.getPatient().getName(), doctor.getId(), doctor.getName(),
                doctor.getDepartment().getName(), entry.getQueueDate(), entry.getStatus(), position,
                entry.getCheckedInAt(), entry.getConsultationStartedAt(), entry.getConsultationCompletedAt());
    }
}
