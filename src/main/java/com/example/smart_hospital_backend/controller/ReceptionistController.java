package com.example.smart_hospital_backend.controller;

import com.example.smart_hospital_backend.dto.AppointmentRequest;
import com.example.smart_hospital_backend.dto.AppointmentResponse;
import com.example.smart_hospital_backend.dto.PatientResponse;
import com.example.smart_hospital_backend.service.AppointmentService;
import com.example.smart_hospital_backend.service.PatientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/receptionist")
public class ReceptionistController {
    private final PatientService patients;
    private final AppointmentService appointments;

    public ReceptionistController(PatientService patients, AppointmentService appointments) {
        this.patients = patients;
        this.appointments = appointments;
    }

    @GetMapping("/patients/search")
    public Page<PatientResponse> searchPatients(
            @RequestParam @NotBlank @Size(max = 100) String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return patients.searchForReception(query, pageable);
    }

    @GetMapping("/patients/{patientId}")
    public PatientResponse getPatient(@PathVariable Long patientId) {
        return patients.findForReception(patientId);
    }

    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse bookAppointment(
            @Valid @RequestBody AppointmentRequest request, Principal principal) {
        return appointments.bookForReceptionist(request, principal.getName());
    }
}
