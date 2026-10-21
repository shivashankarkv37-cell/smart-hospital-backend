package com.example.smart_hospital_backend.service;

import com.example.smart_hospital_backend.dto.AppointmentRequest;
import com.example.smart_hospital_backend.dto.AppointmentResponse;
import com.example.smart_hospital_backend.dto.AdminAppointmentResponse;
import com.example.smart_hospital_backend.entity.Appointment;
import com.example.smart_hospital_backend.entity.AppointmentStatus;
import com.example.smart_hospital_backend.entity.Doctor;
import com.example.smart_hospital_backend.entity.Patient;
import com.example.smart_hospital_backend.entity.User;
import com.example.smart_hospital_backend.entity.UserRole;
import com.example.smart_hospital_backend.exception.AppointmentConflictException;
import com.example.smart_hospital_backend.exception.ForbiddenOperationException;
import com.example.smart_hospital_backend.exception.ResourceNotFoundException;
import com.example.smart_hospital_backend.repository.AppointmentRepository;
import com.example.smart_hospital_backend.repository.DoctorRepository;
import com.example.smart_hospital_backend.repository.PatientRepository;
import com.example.smart_hospital_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {
    private final AppointmentRepository appointments;
    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final UserRepository users;

    public AppointmentService(
            AppointmentRepository appointments,
            PatientRepository patients,
            DoctorRepository doctors,
            UserRepository users) {
        this.appointments = appointments;
        this.patients = patients;
        this.doctors = doctors;
        this.users = users;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AppointmentResponse book(
            AppointmentRequest request,
            String authenticatedEmail) {

        String email = authenticatedEmail.trim().toLowerCase(Locale.ROOT);

        Patient patient = patients.findByEmail(email)
                .orElseThrow(() -> new ForbiddenOperationException(
                        "Authenticated account is not linked to a patient profile"));

        Patient lockedPatient = patients.findByIdForUpdate(patient.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        return createAppointment(request, lockedPatient, authenticatedEmail);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AppointmentResponse bookForReceptionist(AppointmentRequest request, String authenticatedEmail) {
        User creator = findUser(authenticatedEmail);
        if (creator.getRole() != UserRole.RECEPTIONIST && creator.getRole() != UserRole.ADMIN) {
            throw new ForbiddenOperationException("Only reception staff can book for another patient");
        }
        Patient patient = patients.findByIdForUpdate(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return createAppointment(request, patient, authenticatedEmail);
    }

    private AppointmentResponse createAppointment(
            AppointmentRequest request, Patient patient, String authenticatedEmail) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (!LocalDateTime.of(request.appointmentDate(), request.startTime())
                .isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment date and time must be in the future");
        }

        // Lock the doctor row while checking and inserting, serializing bookings for that doctor.
        Doctor doctor = doctors.findByIdForUpdate(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        boolean overlaps = appointments.existsOverlappingAppointment(
                doctor.getId(),
                request.appointmentDate(),
                request.startTime(),
                request.endTime(),
                AppointmentStatus.CANCELLED);
        if (overlaps) {
            throw new AppointmentConflictException("The doctor already has an appointment during this time");
        }

        boolean patientOverlaps = appointments.existsOverlappingPatientAppointment(
                patient.getId(), request.appointmentDate(), request.startTime(), request.endTime(),
                AppointmentStatus.CANCELLED);
        if (patientOverlaps) {
            throw new AppointmentConflictException("The patient already has an appointment during this time");
        }

        Appointment appointment = new Appointment(
                patient,
                doctor,
                request.appointmentDate(),
                request.startTime(),
                request.endTime(),
                request.reason().trim());
        appointment.assignCreatedBy(findUser(authenticatedEmail));
        return AppointmentResponse.from(appointments.save(appointment));
    }

    @Transactional(readOnly = true)
    public Page<AdminAppointmentResponse> searchForAdministration(
            java.time.LocalDate appointmentDate, Long doctorId, Long patientId,
            AppointmentStatus status, Pageable pageable) {
        Pageable boundedPage = PageRequest.of(pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), 100),
                Sort.by(Sort.Order.asc("appointmentDate"), Sort.Order.asc("startTime"), Sort.Order.asc("id")));
        return appointments.searchForAdministration(appointmentDate, doctorId, patientId, status, boundedPage)
                .map(AdminAppointmentResponse::from);
    }

    @Transactional(readOnly = true)
    public AdminAppointmentResponse findForAdministration(Long appointmentId) {
        Appointment appointment = appointments.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        return AdminAppointmentResponse.from(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findMyAppointments(
            String authenticatedEmail) {

        String email = authenticatedEmail.trim().toLowerCase(Locale.ROOT);

        Patient patient = patients.findByEmail(email)
                .orElseThrow(() -> new ForbiddenOperationException(
                        "Authenticated account is not linked to a patient profile"));

        return appointments
                .findByPatientIdOrderByAppointmentDateAscStartTimeAsc(patient.getId())
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findPatientAppointments(
            Long patientId, String authenticatedEmail) {
        String email = authenticatedEmail.trim().toLowerCase(Locale.ROOT);
        Patient patient = patients.findByEmail(email)
                .orElseThrow(() -> new ForbiddenOperationException(
                        "Authenticated account is not linked to a patient profile"));
        if (!patient.getId().equals(patientId)) {
            throw new ForbiddenOperationException("You can only access your own appointments");
        }
        return appointments.findByPatientIdOrderByAppointmentDateAscStartTimeAsc(patient.getId())
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @Transactional
    public AppointmentResponse cancel(
            Long appointmentId, String authenticatedEmail, boolean administrator) {
        Appointment appointment = appointments.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        if (!administrator) {
            ensureOwner(appointment.getPatient(), authenticatedEmail);
        }

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new AppointmentConflictException("Only booked appointments can be cancelled");
        }
        appointment.cancel();
        return AppointmentResponse.from(appointment);
    }

    private void ensureOwner(Patient patient, String authenticatedEmail) {
        String email = authenticatedEmail.trim().toLowerCase(Locale.ROOT);
        Patient authenticatedPatient = patients.findByEmail(email)
                .orElseThrow(() -> new ForbiddenOperationException(
                        "Authenticated account is not linked to a patient profile"));
        if (!authenticatedPatient.getId().equals(patient.getId())) {
            throw new ForbiddenOperationException("You can only access your own appointments");
        }
    }

    private User findUser(String email) {
        return users.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ForbiddenOperationException("Authenticated account was not found"));
    }
}
