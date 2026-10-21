package com.example.smart_hospital_backend.controller;

import com.example.smart_hospital_backend.dto.DoctorRequest;
import com.example.smart_hospital_backend.dto.DoctorResponse;
import com.example.smart_hospital_backend.service.DoctorService;
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
@RequestMapping("/api/admin/doctors")
public class AdminDoctorController {
    private final DoctorService doctors;

    public AdminDoctorController(DoctorService doctors) {
        this.doctors = doctors;
    }

    @GetMapping
    public List<DoctorResponse> findAll() { return doctors.findAll(); }

    @GetMapping("/{doctorId}")
    public DoctorResponse findById(@PathVariable Long doctorId) { return doctors.findById(doctorId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponse create(@Valid @RequestBody DoctorRequest request) { return doctors.create(request); }

    @PutMapping("/{doctorId}")
    public DoctorResponse update(@PathVariable Long doctorId, @Valid @RequestBody DoctorRequest request) {
        return doctors.update(doctorId, request);
    }
}
