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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "patient_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_appointments_patient"))
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", foreignKey = @ForeignKey(name = "fk_appointments_created_by"))
    private User createdBy;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "doctor_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_appointments_doctor"))
    private Doctor doctor;

    @NotNull
    @Column(nullable = false)
    private LocalDate appointmentDate;

    @NotNull
    @Column(nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(nullable = false)
    private LocalTime endTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AppointmentStatus status;

    @Size(max = 500)
    @Column(length = 500)
    private String reason;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Appointment() {}

    public Appointment(
            Patient patient,
            Doctor doctor,
            LocalDate appointmentDate,
            LocalTime startTime,
            LocalTime endTime,
            String reason) {
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
        this.status = AppointmentStatus.BOOKED;
    }

    @PrePersist
    void initializeCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = AppointmentStatus.BOOKED;
        }
    }

    public void cancel() {
        if (status != AppointmentStatus.BOOKED) {
            throw new IllegalStateException("Only booked appointments can be cancelled");
        }
        status = AppointmentStatus.CANCELLED;
    }

    public void assignCreatedBy(User user) {
        this.createdBy = user;
    }

    public void checkIn() {
        if (status != AppointmentStatus.BOOKED) {
            throw new IllegalStateException("Only booked appointments can be checked in");
        }
        status = AppointmentStatus.CHECKED_IN;
    }

    public void startConsultation() {
        if (status != AppointmentStatus.CHECKED_IN) {
            throw new IllegalStateException("Only checked-in appointments can start consultation");
        }
        status = AppointmentStatus.IN_CONSULTATION;
    }

    public void completeConsultation() {
        if (status != AppointmentStatus.IN_CONSULTATION) {
            throw new IllegalStateException("Only appointments in consultation can be completed");
        }
        status = AppointmentStatus.COMPLETED;
    }

    public Long getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
