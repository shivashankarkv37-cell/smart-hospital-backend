package com.example.smart_hospital_backend.controller;

import com.example.smart_hospital_backend.dto.DoctorResponse;
import com.example.smart_hospital_backend.service.DoctorService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorService doctors;

    public DoctorController(DoctorService doctors) {
        this.doctors = doctors;
    }

    @GetMapping
    public List<DoctorResponse> findAll() {
        return doctors.findAll();
    }
}
