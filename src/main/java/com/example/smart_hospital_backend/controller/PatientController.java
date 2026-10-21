package com.example.smart_hospital_backend.controller;

import com.example.smart_hospital_backend.dto.PatientRegistrationRequest;
import com.example.smart_hospital_backend.dto.PatientResponse;
import com.example.smart_hospital_backend.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService patients;

    public PatientController(PatientService patients) {
        this.patients = patients;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse register(@Valid @RequestBody PatientRegistrationRequest request) {
        return patients.register(request);
    }
}
