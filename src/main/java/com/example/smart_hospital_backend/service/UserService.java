package com.example.smart_hospital_backend.service;

import com.example.smart_hospital_backend.dto.RegisterRequest;
import com.example.smart_hospital_backend.dto.UserResponse;
import com.example.smart_hospital_backend.entity.User;
import com.example.smart_hospital_backend.entity.UserRole;
import com.example.smart_hospital_backend.exception.DuplicateEmailException;
import com.example.smart_hospital_backend.repository.UserRepository;
import com.example.smart_hospital_backend.repository.PatientRepository;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository users;
    private final PatientRepository patients;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository users,
            PatientRepository patients,
            @Qualifier("passwordEncoder") PasswordEncoder passwordEncoder) {
        this.users = users;
        this.patients = patients;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email) || patients.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        User user = new User(
                request.fullName().trim(),
                email,
                request.phone().trim(),
                passwordEncoder.encode(request.password()),
                UserRole.PATIENT);
        try {
            return UserResponse.from(users.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
    }
}
