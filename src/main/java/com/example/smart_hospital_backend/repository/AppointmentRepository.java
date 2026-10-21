package com.example.smart_hospital_backend.repository;

import com.example.smart_hospital_backend.entity.Appointment;
import com.example.smart_hospital_backend.entity.AppointmentStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Appointment a where a.id = :appointmentId")
    Optional<Appointment> findByIdForUpdate(@Param("appointmentId") Long appointmentId);

    List<Appointment> findByPatientIdOrderByAppointmentDateAscStartTimeAsc(Long patientId);

    List<Appointment> findByDoctorIdAndAppointmentDateOrderByStartTimeAsc(
            Long doctorId, LocalDate appointmentDate);

    @Query("""
            select (count(a) > 0)
            from Appointment a
            where a.doctor.id = :doctorId
              and a.appointmentDate = :appointmentDate
              and a.status <> :cancelledStatus
              and a.startTime < :requestedEnd
              and a.endTime > :requestedStart
            """)
    boolean existsOverlappingAppointment(
            @Param("doctorId") Long doctorId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("requestedStart") LocalTime requestedStart,
            @Param("requestedEnd") LocalTime requestedEnd,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus);

    @Query("""
            select (count(a) > 0) from Appointment a
            where a.patient.id = :patientId
              and a.appointmentDate = :appointmentDate
              and a.status <> :cancelledStatus
              and a.startTime < :requestedEnd
              and a.endTime > :requestedStart
            """)
    boolean existsOverlappingPatientAppointment(@Param("patientId") Long patientId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("requestedStart") LocalTime requestedStart,
            @Param("requestedEnd") LocalTime requestedEnd,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus);

    @Query(value = """
            select a from Appointment a
            where (:appointmentDate is null or a.appointmentDate = :appointmentDate)
              and (:doctorId is null or a.doctor.id = :doctorId)
              and (:patientId is null or a.patient.id = :patientId)
              and (:status is null or a.status = :status)
            """,
            countQuery = """
            select count(a) from Appointment a
            where (:appointmentDate is null or a.appointmentDate = :appointmentDate)
              and (:doctorId is null or a.doctor.id = :doctorId)
              and (:patientId is null or a.patient.id = :patientId)
              and (:status is null or a.status = :status)
            """)
    Page<Appointment> searchForAdministration(@Param("appointmentDate") LocalDate appointmentDate,
            @Param("doctorId") Long doctorId, @Param("patientId") Long patientId,
            @Param("status") AppointmentStatus status, Pageable pageable);
}
