package com.example.smart_hospital_backend.exception;

public class DuplicateDepartmentException extends RuntimeException {
    public DuplicateDepartmentException() {
        super("A department with this name already exists");
    }
}
