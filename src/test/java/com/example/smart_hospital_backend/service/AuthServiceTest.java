package com.example.smart_hospital_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.smart_hospital_backend.dto.LoginRequest;
import com.example.smart_hospital_backend.dto.LoginResponse;
import com.example.smart_hospital_backend.entity.User;
import com.example.smart_hospital_backend.entity.UserRole;
import com.example.smart_hospital_backend.exception.InvalidCredentialsException;
import com.example.smart_hospital_backend.repository.UserRepository;
import com.example.smart_hospital_backend.repository.PatientRepository;
import com.example.smart_hospital_backend.security.JwtService;
import java.util.Base64;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

class AuthServiceTest {
    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(new byte[32]);
    private final UserRepository users = mock(UserRepository.class);
    private final PatientRepository patients = mock(PatientRepository.class);
    private final PasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
    private final PasswordEncoder legacyEncoder = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                users,
                patients,
                bcryptEncoder,
                legacyEncoder,
                new JwtService(TEST_SECRET, 900));
    }

    @Test
    void validLoginReturnsBearerTokenAndSafeUserDetails() {
        User user = userWithPassword(bcryptEncoder.encode("PatientPassword123"));
        when(users.findByEmail("patient@example.com")).thenReturn(Optional.of(user));

        LoginResponse response = authService.login(
                new LoginRequest(" PATIENT@example.com ", "PatientPassword123"));

        assertEquals("Bearer", response.tokenType());
        assertEquals(900, response.expiresIn());
        assertEquals("patient@example.com", response.user().email());
        assertEquals(UserRole.PATIENT, response.user().role());
        assertEquals(3, response.accessToken().split("\\.").length);
    }

    @Test
    void invalidPasswordReturnsGenericCredentialsError() {
        User user = userWithPassword(bcryptEncoder.encode("CorrectPassword123"));
        when(users.findByEmail("patient@example.com")).thenReturn(Optional.of(user));

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("patient@example.com", "wrong")));
    }

    @Test
    void validLegacyPbkdf2PasswordIsUpgradedToBcrypt() {
        User user = userWithPassword(legacyEncoder.encode("LegacyPassword123"));
        when(users.findByEmail("patient@example.com")).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.login(new LoginRequest("patient@example.com", "LegacyPassword123"));

        assertTrue(user.getPasswordHash().startsWith("$2"));
        assertTrue(bcryptEncoder.matches("LegacyPassword123", user.getPasswordHash()));
        verify(users).save(user);
    }

    @Test
    void unknownEmailReturnsSameGenericCredentialsError() {
        when(users.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("unknown@example.com", "AnyPassword123")));
    }

    private User userWithPassword(String passwordHash) {
        return new User(
                "Test Patient",
                "patient@example.com",
                "+919876543210",
                passwordHash,
                UserRole.PATIENT);
    }
}
