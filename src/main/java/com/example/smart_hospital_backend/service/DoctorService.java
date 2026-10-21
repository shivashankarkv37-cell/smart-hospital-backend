package com.example.smart_hospital_backend.service;

import com.example.smart_hospital_backend.dto.DoctorResponse;
import com.example.smart_hospital_backend.dto.DoctorRequest;
import com.example.smart_hospital_backend.entity.Department;
import com.example.smart_hospital_backend.entity.Doctor;
import com.example.smart_hospital_backend.entity.User;
import com.example.smart_hospital_backend.entity.UserRole;
import com.example.smart_hospital_backend.exception.ConflictException;
import com.example.smart_hospital_backend.exception.ResourceNotFoundException;
import com.example.smart_hospital_backend.repository.DepartmentRepository;
import com.example.smart_hospital_backend.repository.DoctorRepository;
import com.example.smart_hospital_backend.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorService {
    private final DoctorRepository doctors;
    private final DepartmentRepository departments;
    private final UserRepository users;

    public DoctorService(DoctorRepository doctors, DepartmentRepository departments, UserRepository users) {
        this.doctors = doctors;
        this.departments = departments;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> findAll() {
        return doctors.findAll().stream().map(DoctorResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DoctorResponse findById(Long doctorId) {
        return doctors.findById(doctorId).map(DoctorResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    @Transactional
    public DoctorResponse create(DoctorRequest request) {
        Department department = findDepartment(request.departmentId());
        User linkedUser = findDoctorUser(request.userId());
        if (linkedUser != null && doctors.existsByUser_Id(linkedUser.getId())) {
            throw new ConflictException("This user is already linked to a doctor profile");
        }
        return DoctorResponse.from(doctors.saveAndFlush(
                new Doctor(request.name().trim(), request.specialization().trim(), department, linkedUser)));
    }

    @Transactional
    public DoctorResponse update(Long doctorId, DoctorRequest request) {
        Doctor doctor = doctors.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        Department department = findDepartment(request.departmentId());
        User linkedUser = findDoctorUser(request.userId());
        if (linkedUser != null && doctors.existsByUser_IdAndIdNot(linkedUser.getId(), doctorId)) {
            throw new ConflictException("This user is already linked to another doctor profile");
        }
        doctor.updateDetails(request.name().trim(), request.specialization().trim(), department, linkedUser);
        return DoctorResponse.from(doctors.saveAndFlush(doctor));
    }

    private Department findDepartment(Long departmentId) {
        return departments.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    private User findDoctorUser(Long userId) {
        if (userId == null) return null;
        User user = users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));
        if (user.getRole() != UserRole.DOCTOR) {
            throw new IllegalArgumentException("Linked user account must have the DOCTOR role");
        }
        return user;
    }
}
