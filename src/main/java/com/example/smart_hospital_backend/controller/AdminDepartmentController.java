package com.example.smart_hospital_backend.controller;

import com.example.smart_hospital_backend.dto.DepartmentRequest;
import com.example.smart_hospital_backend.dto.DepartmentResponse;
import com.example.smart_hospital_backend.service.DepartmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/departments")
public class AdminDepartmentController {
    private final DepartmentService departments;

    public AdminDepartmentController(DepartmentService departments) {
        this.departments = departments;
    }

    @GetMapping
    public List<DepartmentResponse> findAll() { return departments.findAll(); }

    @GetMapping("/{departmentId}")
    public DepartmentResponse findById(@PathVariable Long departmentId) {
        return departments.findById(departmentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse create(@Valid @RequestBody DepartmentRequest request) {
        return departments.create(request);
    }

    @PutMapping("/{departmentId}")
    public DepartmentResponse update(@PathVariable Long departmentId,
            @Valid @RequestBody DepartmentRequest request) {
        return departments.update(departmentId, request);
    }
}
