package com.example.smart_hospital_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.smart_hospital_backend.dto.AppointmentRequest;
import com.example.smart_hospital_backend.dto.AppointmentResponse;
import com.example.smart_hospital_backend.entity.Appointment;
import com.example.smart_hospital_backend.entity.Department;
import com.example.smart_hospital_backend.entity.Doctor;
import com.example.smart_hospital_backend.entity.Patient;
import com.example.smart_hospital_backend.entity.User;
import com.example.smart_hospital_backend.entity.UserRole;
import com.example.smart_hospital_backend.exception.AppointmentConflictException;
import com.example.smart_hospital_backend.exception.ForbiddenOperationException;
import com.example.smart_hospital_backend.repository.AppointmentRepository;
import com.example.smart_hospital_backend.repository.DoctorRepository;
import com.example.smart_hospital_backend.repository.PatientRepository;
import com.example.smart_hospital_backend.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AppointmentServiceTest {
    private final AppointmentRepository appointments = mock(AppointmentRepository.class);
    private final PatientRepository patients = mock(PatientRepository.class);
    private final DoctorRepository doctors = mock(DoctorRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final Patient patient = new Patient("Patient One", "patient@example.com", "1234567890", "hash");
    private final Department department = new Department("Cardiology", "Heart care");
    private final Doctor doctor = new Doctor("Dr. One", "Cardiology", department, null);
    private final User receptionist = new User("Reception", "desk@example.com", "1234567890",
            "hash", UserRole.RECEPTIONIST);
    private final AppointmentRequest request = new AppointmentRequest(10L, 20L,
            LocalDate.now().plusDays(5), LocalTime.of(10, 0), LocalTime.of(10, 30), "Consultation");
    private AppointmentService service;

    @BeforeEach
    void setUp() {
        service = new AppointmentService(appointments, patients, doctors, users);
        ReflectionTestUtils.setField(patient, "id", 10L);
        ReflectionTestUtils.setField(department, "id", 3L);
        ReflectionTestUtils.setField(doctor, "id", 20L);
        when(users.findByEmail("desk@example.com")).thenReturn(Optional.of(receptionist));
        when(patients.findByIdForUpdate(10L)).thenReturn(Optional.of(patient));
        when(doctors.findByIdForUpdate(20L)).thenReturn(Optional.of(doctor));
        when(appointments.existsOverlappingAppointment(any(), any(), any(), any(), any())).thenReturn(false);
        when(appointments.existsOverlappingPatientAppointment(any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(appointments.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 50L);
            return saved;
        });
    }

    @Test
    void receptionistCanBookExistingPatientAndCreatorIsRecorded() {
        AppointmentResponse response = service.bookForReceptionist(request, "desk@example.com");

        assertEquals(50L, response.id());
        assertEquals(10L, response.patientId());
        assertEquals(20L, response.doctor().id());
        assertEquals(receptionist, appointmentsArgumentCreatedBy());
    }

    @Test
    void rejectsDoctorOverlap() {
        when(appointments.existsOverlappingAppointment(any(), any(), any(), any(), any())).thenReturn(true);

        assertThrows(AppointmentConflictException.class,
                () -> service.bookForReceptionist(request, "desk@example.com"));
    }

    @Test
    void rejectsPatientOverlap() {
        when(appointments.existsOverlappingPatientAppointment(any(), any(), any(), any(), any()))
                .thenReturn(true);

        assertThrows(AppointmentConflictException.class,
                () -> service.bookForReceptionist(request, "desk@example.com"));
    }

    @Test
    void patientCanCancelOwnBookedAppointment() {
        Appointment appointment = new Appointment(patient, doctor, request.appointmentDate(),
                request.startTime(), request.endTime(), request.reason());
        ReflectionTestUtils.setField(appointment, "id", 70L);
        when(appointments.findById(70L)).thenReturn(Optional.of(appointment));
        when(patients.findByEmail("patient@example.com")).thenReturn(Optional.of(patient));

        AppointmentResponse response = service.cancel(70L, "patient@example.com", false);

        assertEquals(com.example.smart_hospital_backend.entity.AppointmentStatus.CANCELLED,
                response.status());
    }

    @Test
    void patientCannotCancelAnotherPatientsAppointment() {
        Appointment appointment = new Appointment(patient, doctor, request.appointmentDate(),
                request.startTime(), request.endTime(), request.reason());
        ReflectionTestUtils.setField(appointment, "id", 71L);
        when(appointments.findById(71L)).thenReturn(Optional.of(appointment));
        when(patients.findByEmail("other@example.com"))
                .thenReturn(Optional.of(new Patient("Other", "other@example.com", "1234567890", "hash")));

        assertThrows(ForbiddenOperationException.class,
                () -> service.cancel(71L, "other@example.com", false));
    }

    @Test
    void patientRetrievesAppointmentsUsingPatientIdResolvedFromEmail() {
        ReflectionTestUtils.setField(patient, "id", 7L);
        when(patients.findByEmail("patient@example.com")).thenReturn(Optional.of(patient));

        var result = service.findMyAppointments(" PATIENT@example.com ");

        assertEquals(java.util.List.of(), result);
        verify(appointments).findByPatientIdOrderByAppointmentDateAscStartTimeAsc(7L);
    }

    @Test
    void patientWithoutLinkedProfileCannotRetrieveAppointments() {
        when(patients.findByEmail("patient@example.com")).thenReturn(Optional.empty());

        assertThrows(ForbiddenOperationException.class,
                () -> service.findMyAppointments("patient@example.com"));
        org.mockito.Mockito.verifyNoInteractions(appointments);
    }

    @Test
    void administratorCanCancelBookedAppointment() {
        Appointment appointment = new Appointment(patient, doctor, request.appointmentDate(),
                request.startTime(), request.endTime(), request.reason());
        ReflectionTestUtils.setField(appointment, "id", 72L);
        when(appointments.findById(72L)).thenReturn(Optional.of(appointment));

        AppointmentResponse response = service.cancel(72L, "admin@example.com", true);

        assertEquals(com.example.smart_hospital_backend.entity.AppointmentStatus.CANCELLED,
                response.status());
    }

    private User appointmentsArgumentCreatedBy() {
        var captor = org.mockito.ArgumentCaptor.forClass(Appointment.class);
        org.mockito.Mockito.verify(appointments).save(captor.capture());
        return captor.getValue().getCreatedBy();
    }
}
