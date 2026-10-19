package com.example.smart_hospital_backend.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() { super("An account with this email already exists"); }
}
