package com.example.smart_hospital_backend.service;

import com.example.smart_hospital_backend.dto.AuthenticatedUserResponse;
import com.example.smart_hospital_backend.dto.LoginRequest;
import com.example.smart_hospital_backend.dto.LoginResponse;
import com.example.smart_hospital_backend.entity.User;
import com.example.smart_hospital_backend.entity.Patient;
import com.example.smart_hospital_backend.entity.UserRole;
import com.example.smart_hospital_backend.exception.InvalidCredentialsException;
import com.example.smart_hospital_backend.repository.UserRepository;
import com.example.smart_hospital_backend.repository.PatientRepository;
import com.example.smart_hospital_backend.security.JwtService;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PatientRepository patients;
    private final PasswordEncoder passwordEncoder;
    private final PasswordEncoder legacyPasswordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository users,
            PatientRepository patients,
            @Qualifier("passwordEncoder") PasswordEncoder passwordEncoder,
            @Qualifier("legacyPasswordEncoder") PasswordEncoder legacyPasswordEncoder,
            JwtService jwtService) {
        this.users = users;
        this.patients = patients;
        this.passwordEncoder = passwordEncoder;
        this.legacyPasswordEncoder = legacyPasswordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = users.findByEmail(email).orElseGet(() -> createUserForLegacyPatient(email, request.password()));

        if (!passwordMatchesAndUpgrade(user, request.password())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtService.getExpirationSeconds(),
                AuthenticatedUserResponse.from(user)
        );
    }

    private User createUserForLegacyPatient(String email, String rawPassword) {
        Patient patient = patients.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        String hash = patient.getPasswordHash();
        boolean valid = isBcryptHash(hash)
                ? matches(passwordEncoder, rawPassword, hash)
                : matches(legacyPasswordEncoder, rawPassword, hash);
        if (!valid) {
            throw new InvalidCredentialsException();
        }
        if (!isBcryptHash(hash)) {
            hash = passwordEncoder.encode(rawPassword);
        }
        User user = new User(patient.getName(), email, patient.getPhone(), hash, UserRole.PATIENT);
        return users.saveAndFlush(user);
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse currentUser(String authenticatedEmail) {
        String email = normalizeEmail(authenticatedEmail);
        User user = users.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        return AuthenticatedUserResponse.from(user);
    }

    private boolean passwordMatchesAndUpgrade(User user, String rawPassword) {
        String storedHash = user.getPasswordHash();
        if (isBcryptHash(storedHash)) {
            return matches(passwordEncoder, rawPassword, storedHash);
        }

        if (!matches(legacyPasswordEncoder, rawPassword, storedHash)) {
            return false;
        }

        user.updatePasswordHash(passwordEncoder.encode(rawPassword));
        users.save(user);
        return true;
    }

    private boolean matches(PasswordEncoder encoder, String rawPassword, String storedHash) {
        try {
            return encoder.matches(rawPassword, storedHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean isBcryptHash(String encodedPassword) {
        return encodedPassword.startsWith("$2a$")
                || encodedPassword.startsWith("$2b$")
                || encodedPassword.startsWith("$2y$");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
