package com.example.smart_hospital_backend.service;

import com.example.smart_hospital_backend.dto.DepartmentRequest;
import com.example.smart_hospital_backend.dto.DepartmentResponse;
import com.example.smart_hospital_backend.entity.Department;
import com.example.smart_hospital_backend.exception.DuplicateDepartmentException;
import com.example.smart_hospital_backend.exception.ResourceNotFoundException;
import com.example.smart_hospital_backend.repository.DepartmentRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentService {
    private final DepartmentRepository departments;

    public DepartmentService(DepartmentRepository departments) {
        this.departments = departments;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departments.findAll().stream().map(DepartmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long departmentId) {
        return departments.findById(departmentId).map(DepartmentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        String name = request.name().trim();
        if (departments.existsByNameIgnoreCase(name)) {
            throw new DuplicateDepartmentException();
        }

        try {
            Department department = new Department(name, normalizeDescription(request.description()));
            return DepartmentResponse.from(departments.saveAndFlush(department));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateDepartmentException();
        }
    }

    @Transactional
    public DepartmentResponse update(Long departmentId, DepartmentRequest request) {
        Department department = departments.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        String name = request.name().trim();
        if (departments.existsByNameIgnoreCaseAndIdNot(name, departmentId)) {
            throw new DuplicateDepartmentException();
        }
        department.updateDetails(name, normalizeDescription(request.description()));
        try {
            return DepartmentResponse.from(departments.saveAndFlush(department));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateDepartmentException();
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }
}
