package com.example.smart_hospital_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.smart_hospital_backend.dto.DepartmentRequest;
import com.example.smart_hospital_backend.dto.DoctorRequest;
import com.example.smart_hospital_backend.entity.Department;
import com.example.smart_hospital_backend.entity.Doctor;
import com.example.smart_hospital_backend.exception.DuplicateDepartmentException;
import com.example.smart_hospital_backend.repository.DepartmentRepository;
import com.example.smart_hospital_backend.repository.DoctorRepository;
import com.example.smart_hospital_backend.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AdminManagementServiceTest {
    private final DepartmentRepository departments = mock(DepartmentRepository.class);
    private final DoctorRepository doctors = mock(DoctorRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private DepartmentService departmentService;
    private DoctorService doctorService;
    private Department cardiology;
    private Department neurology;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        departmentService = new DepartmentService(departments);
        doctorService = new DoctorService(doctors, departments, users);
        cardiology = new Department("Cardiology", "Heart care");
        neurology = new Department("Neurology", "Nervous system care");
        ReflectionTestUtils.setField(cardiology, "id", 1L);
        ReflectionTestUtils.setField(neurology, "id", 2L);
        doctor = new Doctor("Dr. Existing", "Cardiology", cardiology, null);
        ReflectionTestUtils.setField(doctor, "id", 12L);
        when(departments.findById(1L)).thenReturn(Optional.of(cardiology));
        when(departments.findById(2L)).thenReturn(Optional.of(neurology));
        when(departments.saveAndFlush(any(Department.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(doctors.saveAndFlush(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void adminDoctorCreateAndUpdateUseExistingDepartmentAndKeepId() {
        DoctorRequest create = new DoctorRequest("Dr. New", "Neurology", 2L, null);
        var created = doctorService.create(create);

        assertEquals("Dr. New", created.name());
        assertEquals("Neurology", created.department().name());

        when(doctors.findById(12L)).thenReturn(Optional.of(doctor));
        DoctorRequest update = new DoctorRequest("Dr. Updated", "Neurosurgery", 2L, null);
        var updated = doctorService.update(12L, update);

        assertEquals(12L, updated.id());
        assertEquals("Dr. Updated", updated.name());
        assertEquals("Neurosurgery", updated.specialization());
        assertEquals(2L, updated.department().id());
    }

    @Test
    void departmentUpdatePreservesIdentityAndRejectsDuplicateNames() {
        when(departments.findById(1L)).thenReturn(Optional.of(cardiology));
        when(departments.existsByNameIgnoreCaseAndIdNot("Neuro", 1L)).thenReturn(false);

        var updated = departmentService.update(1L, new DepartmentRequest("Neuro", "Updated"));

        assertEquals(1L, updated.id());
        assertEquals("Neuro", updated.name());

        when(departments.existsByNameIgnoreCaseAndIdNot("Neurology", 1L)).thenReturn(true);
        assertThrows(DuplicateDepartmentException.class,
                () -> departmentService.update(1L, new DepartmentRequest("Neurology", null)));
    }
}
