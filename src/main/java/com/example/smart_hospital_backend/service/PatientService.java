package com.example.smart_hospital_backend.service;

import com.example.smart_hospital_backend.dto.PatientRegistrationRequest;
import com.example.smart_hospital_backend.dto.PatientResponse;
import com.example.smart_hospital_backend.entity.Patient;
import com.example.smart_hospital_backend.entity.User;
import com.example.smart_hospital_backend.entity.UserRole;
import com.example.smart_hospital_backend.exception.DuplicateEmailException;
import com.example.smart_hospital_backend.repository.PatientRepository;
import com.example.smart_hospital_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {
    private final PatientRepository patients;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public PatientService(
            PatientRepository patients,
            UserRepository users,
            @Qualifier("passwordEncoder") PasswordEncoder passwordEncoder) {
        this.patients = patients;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PatientResponse register(PatientRegistrationRequest request) {
        String email = request.email().trim().toLowerCase();
        if (patients.existsByEmail(email) || users.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        Patient patient = new Patient(
                request.name().trim(),
                email,
                request.phone().trim(),
                passwordEncoder.encode(request.password()));
        try {
            Patient savedPatient = patients.saveAndFlush(patient);
            users.saveAndFlush(new User(
                    patient.getName(), email, patient.getPhone(),
                    patient.getPasswordHash(), UserRole.PATIENT));
            return PatientResponse.from(savedPatient);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> searchForReception(String query, Pageable pageable) {
        String term = query.trim();
        Long patientId = null;
        try {
            patientId = Long.valueOf(term);
        } catch (NumberFormatException ignored) {
            // Non-numeric search terms match name, email, and phone.
        }
        Pageable boundedPage = PageRequest.of(pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), 100), Sort.by("id").ascending());
        return patients.searchForReception(patientId, term, boundedPage).map(PatientResponse::from);
    }

    @Transactional(readOnly = true)
    public PatientResponse findForReception(Long patientId) {
        Patient patient = patients.findById(patientId)
                .orElseThrow(() -> new com.example.smart_hospital_backend.exception.ResourceNotFoundException(
                        "Patient not found"));
        return PatientResponse.from(patient);
    }
}
