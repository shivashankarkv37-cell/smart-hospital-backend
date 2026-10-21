package com.example.smart_hospital_backend.controller;

import com.example.smart_hospital_backend.dto.AdminAppointmentResponse;
import com.example.smart_hospital_backend.entity.AppointmentStatus;
import com.example.smart_hospital_backend.service.AppointmentService;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/admin/appointments")
public class AdminAppointmentController {
    private final AppointmentService appointments;

    public AdminAppointmentController(AppointmentService appointments) {
        this.appointments = appointments;
    }

    @GetMapping
    public Page<AdminAppointmentResponse> search(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) @Positive Long doctorId,
            @RequestParam(required = false) @Positive Long patientId,
            @RequestParam(required = false) AppointmentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return appointments.searchForAdministration(date, doctorId, patientId, status, pageable);
    }

    @GetMapping("/{appointmentId}")
    public AdminAppointmentResponse findById(@PathVariable Long appointmentId) {
        return appointments.findForAdministration(appointmentId);
    }
}
