package com.example.smart_hospital_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.smart_hospital_backend.dto.RegisterRequest;
import com.example.smart_hospital_backend.entity.User;
import com.example.smart_hospital_backend.entity.UserRole;
import com.example.smart_hospital_backend.repository.UserRepository;
import com.example.smart_hospital_backend.repository.PatientRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UserServiceTest {
    @Test
    void registrationAlwaysStoresPatientWithBcryptHash() {
        UserRepository users = mock(UserRepository.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        when(users.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        UserService userService = new UserService(users, mock(PatientRepository.class), passwordEncoder);
        RegisterRequest request = new RegisterRequest(
                "Rahul Sharma",
                "RAHUL@example.com",
                "+919876543210",
                "StrongPassword123",
                null);

        var response = userService.register(request);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(users).saveAndFlush(savedUser.capture());
        assertEquals(UserRole.PATIENT, savedUser.getValue().getRole());
        assertTrue(savedUser.getValue().getEmail().equals("rahul@example.com"));
        assertTrue(savedUser.getValue().getPasswordHash().startsWith("$2"));
        assertTrue(passwordEncoder.matches(
                "StrongPassword123", savedUser.getValue().getPasswordHash()));
        assertTrue(java.util.Arrays.stream(response.getClass().getRecordComponents())
                .noneMatch(component -> component.getName().toLowerCase().contains("password")));
    }

    @Test
    void registrationDtoRejectsPrivilegedSelfAssignedRole() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();
            RegisterRequest request = new RegisterRequest(
                    "Rahul Sharma",
                    "rahul@example.com",
                    "+919876543210",
                    "StrongPassword123",
                    "ADMIN");

            Set<?> violations = validator.validate(request);

            assertTrue(violations.stream().anyMatch(violation ->
                    violation.toString().contains("Only patient accounts can be registered")));
        }
    }
}
