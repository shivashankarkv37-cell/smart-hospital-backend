package com.example.smart_hospital_backend.controller;

import com.example.smart_hospital_backend.dto.AppointmentRequest;
import com.example.smart_hospital_backend.dto.AppointmentResponse;
import com.example.smart_hospital_backend.service.AppointmentService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointments;

    public AppointmentController(AppointmentService appointments) {
        this.appointments = appointments;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> book(
        @Valid @RequestBody AppointmentRequest request, Principal principal) {
        AppointmentResponse response = appointments.book(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/patient/{patientId}")
    public List<AppointmentResponse> findPatientAppointments(
            @PathVariable Long patientId, Principal principal) {
        return appointments.findPatientAppointments(patientId, principal.getName());
    }

    @PatchMapping("/{appointmentId}/cancel")
    public AppointmentResponse cancel(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserDetails authenticatedUser) {
        boolean isAdmin = authenticatedUser.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return appointments.cancel(appointmentId, authenticatedUser.getUsername(), isAdmin);
    }
    @GetMapping("/mine")
    public List<AppointmentResponse> findMyAppointments(Principal principal) {
        return appointments.findMyAppointments(principal.getName());
    }
}
