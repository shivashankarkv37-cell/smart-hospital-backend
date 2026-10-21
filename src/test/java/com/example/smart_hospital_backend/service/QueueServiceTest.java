package com.example.smart_hospital_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.smart_hospital_backend.dto.QueueEntryResponse;
import com.example.smart_hospital_backend.entity.Appointment;
import com.example.smart_hospital_backend.entity.AppointmentStatus;
import com.example.smart_hospital_backend.entity.Department;
import com.example.smart_hospital_backend.entity.Doctor;
import com.example.smart_hospital_backend.entity.Patient;
import com.example.smart_hospital_backend.entity.QueueEntry;
import com.example.smart_hospital_backend.entity.QueueStatus;
import com.example.smart_hospital_backend.entity.User;
import com.example.smart_hospital_backend.entity.UserRole;
import com.example.smart_hospital_backend.exception.AppointmentConflictException;
import com.example.smart_hospital_backend.exception.ForbiddenOperationException;
import com.example.smart_hospital_backend.repository.AppointmentRepository;
import com.example.smart_hospital_backend.repository.DoctorRepository;
import com.example.smart_hospital_backend.repository.PatientRepository;
import com.example.smart_hospital_backend.repository.QueueEntryRepository;
import com.example.smart_hospital_backend.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class QueueServiceTest {
    private final QueueEntryRepository queueEntries = mock(QueueEntryRepository.class);
    private final AppointmentRepository appointments = mock(AppointmentRepository.class);
    private final DoctorRepository doctors = mock(DoctorRepository.class);
    private final PatientRepository patients = mock(PatientRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final Patient patient = mock(Patient.class);
    private final Department department = mock(Department.class);
    private final Doctor doctor = mock(Doctor.class);
    private Appointment appointment;
    private QueueService service;

    @BeforeEach
    void setUp() {
        service = new QueueService(queueEntries, appointments, doctors, patients, users);
        when(patient.getName()).thenReturn("Test Patient");
        when(doctor.getId()).thenReturn(2L);
        when(doctor.getName()).thenReturn("Dr. Example");
        when(doctor.getDepartment()).thenReturn(department);
        when(department.getName()).thenReturn("Cardiology");
        appointment = new Appointment(patient, doctor, LocalDate.now(),
                LocalTime.of(10, 0), LocalTime.of(10, 30), "Consultation");
        ReflectionTestUtils.setField(appointment, "id", 10L);
        when(appointments.findByIdForUpdate(10L)).thenReturn(Optional.of(appointment));
        when(doctors.findByIdForUpdate(2L)).thenReturn(Optional.of(doctor));
        when(queueEntries.countByDoctor_IdAndQueueDate(2L, LocalDate.now())).thenReturn(0L);
        when(queueEntries.countWaitingAhead(any(), any(), any(), any(), any())).thenReturn(0L);
        when(queueEntries.saveAndFlush(any(QueueEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void checkInCreatesTokenAndMovesAppointmentToCheckedIn() {
        when(queueEntries.existsByAppointment_Id(10L)).thenReturn(false);

        QueueEntryResponse response = service.checkIn(10L);

        assertEquals("A001", response.tokenNumber());
        assertEquals(QueueStatus.WAITING, response.status());
        assertEquals(1, response.queuePosition());
        assertEquals(AppointmentStatus.CHECKED_IN, appointment.getStatus());
    }

    @Test
    void duplicateCheckInIsRejected() {
        when(queueEntries.existsByAppointment_Id(10L)).thenReturn(true);

        assertThrows(AppointmentConflictException.class, () -> service.checkIn(10L));
        assertEquals(AppointmentStatus.BOOKED, appointment.getStatus());
    }

    @Test
    void futureOrCancelledAppointmentCannotBeCheckedIn() {
        ReflectionTestUtils.setField(appointment, "appointmentDate", LocalDate.now().plusDays(1));
        assertThrows(AppointmentConflictException.class, () -> service.checkIn(10L));
        assertEquals(AppointmentStatus.BOOKED, appointment.getStatus());
    }

    @Test
    void assignedDoctorCanStartAndCompleteConsultation() {
        appointment.checkIn();
        QueueEntry entry = new QueueEntry("A001", appointment, LocalDate.now(),
                java.time.LocalDateTime.now());
        ReflectionTestUtils.setField(entry, "id", 50L);
        User doctorUser = new User("Doctor", "doctor@example.com", "1234567890", "hash", UserRole.DOCTOR);
        when(users.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctors.findByUserEmail("doctor@example.com")).thenReturn(Optional.of(doctor));
        when(queueEntries.findByIdForUpdate(50L)).thenReturn(Optional.of(entry));

        QueueEntryResponse started = service.startConsultation(50L, "doctor@example.com");
        assertEquals(QueueStatus.IN_CONSULTATION, started.status());
        assertEquals(AppointmentStatus.IN_CONSULTATION, appointment.getStatus());
        QueueEntryResponse completed = service.completeConsultation(50L, "doctor@example.com");

        assertEquals(QueueStatus.COMPLETED, completed.status());
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
    }

    @Test
    void doctorCannotManageAnotherDoctorsQueue() {
        appointment.checkIn();
        QueueEntry entry = new QueueEntry("A001", appointment, LocalDate.now(),
                java.time.LocalDateTime.now());
        ReflectionTestUtils.setField(entry, "id", 50L);
        Doctor otherDoctor = mock(Doctor.class);
        when(otherDoctor.getId()).thenReturn(3L);
        User doctorUser = new User("Other Doctor", "other@example.com", "1234567890", "hash", UserRole.DOCTOR);
        when(users.findByEmail("other@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctors.findByUserEmail("other@example.com")).thenReturn(Optional.of(otherDoctor));
        when(queueEntries.findByIdForUpdate(50L)).thenReturn(Optional.of(entry));

        assertThrows(ForbiddenOperationException.class,
                () -> service.startConsultation(50L, "other@example.com"));
        assertEquals(QueueStatus.WAITING, entry.getStatus());
    }
}
