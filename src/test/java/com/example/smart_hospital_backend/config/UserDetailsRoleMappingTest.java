package com.example.smart_hospital_backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.smart_hospital_backend.entity.User;
import com.example.smart_hospital_backend.entity.UserRole;
import com.example.smart_hospital_backend.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetailsService;

class UserDetailsRoleMappingTest {
    @Test
    void allExistingRolesBecomeSpringSecurityRoleAuthorities() {
        UserRepository users = mock(UserRepository.class);
        SecurityConfig config = new SecurityConfig();
        UserDetailsService service = config.userDetailsService(users);

        for (UserRole role : UserRole.values()) {
            User user = new User("Test User", "user@example.com", "9876543210", "hash", role);
            when(users.findByEmail("user@example.com")).thenReturn(Optional.of(user));

            var details = service.loadUserByUsername(" USER@example.com ");

            assertEquals("ROLE_" + role.name(),
                    details.getAuthorities().iterator().next().getAuthority());
        }
    }
}
