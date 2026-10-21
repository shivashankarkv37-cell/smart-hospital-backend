package com.example.smart_hospital_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_entries", uniqueConstraints = {
    @UniqueConstraint(name = "uk_queue_appointment", columnNames = "appointment_id"),
    @UniqueConstraint(name = "uk_queue_doctor_date_token", columnNames = {"doctor_id", "queue_date", "token_number"})
})
public class QueueEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_number", nullable = false, length = 20)
    private String tokenNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_queue_appointment"))
    private Appointment appointment;

    // Duplicated from Appointment to enforce per-doctor/day token uniqueness in the database.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_queue_doctor"))
    private Doctor doctor;

    @Column(name = "queue_date", nullable = false)
    private LocalDate queueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QueueStatus status;

    @Column(nullable = false)
    private LocalDateTime checkedInAt;

    private LocalDateTime consultationStartedAt;
    private LocalDateTime consultationCompletedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected QueueEntry() {}

    public QueueEntry(String tokenNumber, Appointment appointment, LocalDate queueDate, LocalDateTime checkedInAt) {
        this.tokenNumber = tokenNumber;
        this.appointment = appointment;
        this.doctor = appointment.getDoctor();
        this.queueDate = queueDate;
        this.status = QueueStatus.WAITING;
        this.checkedInAt = checkedInAt;
    }

    @PrePersist
    void initializeCreatedAt() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public void startConsultation(LocalDateTime startedAt) {
        if (status != QueueStatus.WAITING) {
            throw new IllegalStateException("Only waiting queue entries can start consultation");
        }
        status = QueueStatus.IN_CONSULTATION;
        consultationStartedAt = startedAt;
    }

    public void completeConsultation(LocalDateTime completedAt) {
        if (status != QueueStatus.IN_CONSULTATION) {
            throw new IllegalStateException("Only queue entries in consultation can be completed");
        }
        status = QueueStatus.COMPLETED;
        consultationCompletedAt = completedAt;
    }

    public Long getId() { return id; }
    public String getTokenNumber() { return tokenNumber; }
    public Appointment getAppointment() { return appointment; }
    public Doctor getDoctor() { return doctor; }
    public LocalDate getQueueDate() { return queueDate; }
    public QueueStatus getStatus() { return status; }
    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public LocalDateTime getConsultationStartedAt() { return consultationStartedAt; }
    public LocalDateTime getConsultationCompletedAt() { return consultationCompletedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
