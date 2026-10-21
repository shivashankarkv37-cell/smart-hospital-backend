package com.example.smart_hospital_backend.dto;
import com.example.smart_hospital_backend.entity.Department;
public record DepartmentResponse(Long id, String name, String description) {
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(), department.getName(), department.getDescription());
    }
}
