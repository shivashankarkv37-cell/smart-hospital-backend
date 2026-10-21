package com.example.smart_hospital_backend.service;

import com.example.smart_hospital_backend.dto.QueueEntryResponse;
import com.example.smart_hospital_backend.entity.Appointment;
import com.example.smart_hospital_backend.entity.AppointmentStatus;
import com.example.smart_hospital_backend.entity.Doctor;
import com.example.smart_hospital_backend.entity.Patient;
import com.example.smart_hospital_backend.entity.QueueEntry;
import com.example.smart_hospital_backend.entity.QueueStatus;
import com.example.smart_hospital_backend.entity.User;
import com.example.smart_hospital_backend.entity.UserRole;
import com.example.smart_hospital_backend.exception.AppointmentConflictException;
import com.example.smart_hospital_backend.exception.ForbiddenOperationException;
import com.example.smart_hospital_backend.exception.ResourceNotFoundException;
import com.example.smart_hospital_backend.repository.AppointmentRepository;
import com.example.smart_hospital_backend.repository.DoctorRepository;
import com.example.smart_hospital_backend.repository.PatientRepository;
import com.example.smart_hospital_backend.repository.QueueEntryRepository;
import com.example.smart_hospital_backend.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QueueService {
    private static final Set<QueueStatus> ACTIVE_STATUSES = Set.of(
            QueueStatus.WAITING, QueueStatus.IN_CONSULTATION);

    private final QueueEntryRepository queueEntries;
    private final AppointmentRepository appointments;
    private final DoctorRepository doctors;
    private final PatientRepository patients;
    private final UserRepository users;

    public QueueService(QueueEntryRepository queueEntries, AppointmentRepository appointments,
            DoctorRepository doctors, PatientRepository patients, UserRepository users) {
        this.queueEntries = queueEntries;
        this.appointments = appointments;
        this.doctors = doctors;
        this.patients = patients;
        this.users = users;
    }

    @Transactional
    public QueueEntryResponse checkIn(Long appointmentId) {
        Appointment appointment = appointments.findByIdForUpdate(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        LocalDate today = LocalDate.now();
        if (appointment.getStatus() != AppointmentStatus.BOOKED
                || !appointment.getAppointmentDate().equals(today)) {
            throw new AppointmentConflictException(
                    "Only booked appointments scheduled for today can be checked in");
        }

        // This lock serializes token allocation for this doctor and date across application instances.
        Doctor doctor = doctors.findByIdForUpdate(appointment.getDoctor().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        if (queueEntries.existsByAppointment_Id(appointmentId)) {
            throw new AppointmentConflictException("This appointment has already been checked in");
        }

        long nextToken = queueEntries.countByDoctor_IdAndQueueDate(doctor.getId(), today) + 1;
        String token = "A" + String.format(Locale.ROOT, "%03d", nextToken);
        LocalDateTime checkedInAt = LocalDateTime.now();
        appointment.checkIn();
        QueueEntry entry = queueEntries.saveAndFlush(
                new QueueEntry(token, appointment, today, checkedInAt));
        return QueueEntryResponse.from(entry, waitingPosition(entry));
    }

    @Transactional(readOnly = true)
    public List<QueueEntryResponse> currentQueue(String email) {
        User user = user(email);
        List<QueueEntry> entries;
        if (user.getRole() == UserRole.DOCTOR) {
            Doctor doctor = doctorFor(email);
            entries = queueEntries.findByDoctor_IdAndQueueDateAndStatusInOrderByCheckedInAtAscIdAsc(
                    doctor.getId(), LocalDate.now(), ACTIVE_STATUSES);
        } else if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.RECEPTIONIST) {
            entries = queueEntries.findByQueueDateAndStatusInOrderByCheckedInAtAscIdAsc(
                    LocalDate.now(), ACTIVE_STATUSES);
        } else {
            throw new ForbiddenOperationException("This account cannot view the current hospital queue");
        }
        return entries.stream().map(entry -> QueueEntryResponse.from(entry,
                entry.getStatus() == QueueStatus.WAITING ? waitingPosition(entry) : null)).toList();
    }

    @Transactional(readOnly = true)
    public QueueEntryResponse myStatus(String email) {
        Patient patient = patients.findByEmail(normalize(email))
                .orElseThrow(() -> new ForbiddenOperationException(
                        "Authenticated account is not linked to a patient profile"));
        QueueEntry entry = queueEntries
                .findFirstByAppointment_Patient_IdAndQueueDateAndStatusInOrderByCheckedInAtDesc(
                        patient.getId(), LocalDate.now(), ACTIVE_STATUSES)
                .orElseThrow(() -> new ResourceNotFoundException("No active queue entry found for today"));
        return QueueEntryResponse.from(entry,
                entry.getStatus() == QueueStatus.WAITING ? waitingPosition(entry) : null);
    }

    @Transactional
    public QueueEntryResponse startConsultation(Long id, String email) {
        QueueEntry entry = findEntry(id);
        verifyAssignedDoctorOrAdmin(email, entry);
        if (entry.getStatus() != QueueStatus.WAITING
                || entry.getAppointment().getStatus() != AppointmentStatus.CHECKED_IN) {
            throw new AppointmentConflictException("Only waiting, checked-in appointments can start consultation");
        }
        LocalDateTime now = LocalDateTime.now();
        entry.startConsultation(now);
        entry.getAppointment().startConsultation();
        return QueueEntryResponse.from(entry, null);
    }

    @Transactional
    public QueueEntryResponse completeConsultation(Long id, String email) {
        QueueEntry entry = findEntry(id);
        verifyAssignedDoctorOrAdmin(email, entry);
        if (entry.getStatus() != QueueStatus.IN_CONSULTATION
                || entry.getAppointment().getStatus() != AppointmentStatus.IN_CONSULTATION) {
            throw new AppointmentConflictException("Only appointments in consultation can be completed");
        }
        LocalDateTime now = LocalDateTime.now();
        entry.completeConsultation(now);
        entry.getAppointment().completeConsultation();
        return QueueEntryResponse.from(entry, null);
    }

    private int waitingPosition(QueueEntry entry) {
        return Math.toIntExact(queueEntries.countWaitingAhead(entry.getDoctor().getId(), entry.getQueueDate(),
                QueueStatus.WAITING, entry.getCheckedInAt(), entry.getId()) + 1);
    }

    private QueueEntry findEntry(Long id) {
        return queueEntries.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Queue entry not found"));
    }

    private void verifyAssignedDoctorOrAdmin(String email, QueueEntry entry) {
        User user = user(email);
        if (user.getRole() == UserRole.ADMIN) return;
        if (user.getRole() != UserRole.DOCTOR
                || !doctorFor(email).getId().equals(entry.getDoctor().getId())) {
            throw new ForbiddenOperationException("You are not authorized to manage this doctor's queue");
        }
    }

    private Doctor doctorFor(String email) {
        return doctors.findByUserEmail(normalize(email))
                .orElseThrow(() -> new ForbiddenOperationException(
                        "Authenticated doctor account is not linked to a doctor profile"));
    }

    private User user(String email) {
        return users.findByEmail(normalize(email))
                .orElseThrow(() -> new ForbiddenOperationException("Authenticated account was not found"));
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
