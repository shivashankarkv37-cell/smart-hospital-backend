package com.example.smart_hospital_backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;

class JwtAuthenticationFilterTest {
    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(new byte[32]);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validBearerTokenAuthenticatesWithDatabaseAuthorities() throws Exception {
        JwtService jwtService = new JwtService(TEST_SECRET, 900);
        UserDetailsService users = mock(UserDetailsService.class);
        var details = User.withUsername("patient@example.com")
                .password("not-used")
                .authorities(new SimpleGrantedAuthority("ROLE_PATIENT"))
                .build();
        when(users.loadUserByUsername("patient@example.com")).thenReturn(details);
        JwtAuthenticationFilter filter = filter(jwtService, users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwtService.generateAccessToken("patient@example.com"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger();

        filter.doFilterInternal(request, response, (servletRequest, servletResponse) -> chainCalls.incrementAndGet());

        assertEquals(1, chainCalls.get());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("patient@example.com", SecurityContextHolder.getContext()
                .getAuthentication().getName());
        assertEquals("ROLE_PATIENT", SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void missingTokenContinuesWithoutAuthenticating() throws Exception {
        UserDetailsService users = mock(UserDetailsService.class);
        JwtAuthenticationFilter filter = filter(new JwtService(TEST_SECRET, 900), users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger();

        filter.doFilterInternal(request, response, (servletRequest, servletResponse) -> chainCalls.incrementAndGet());

        assertEquals(1, chainCalls.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void invalidBearerTokenDoesNotBlockPublicDoctorListing() throws Exception {
        byte[] otherKey = new byte[32];
        otherKey[0] = 1;
        JwtService issuer = new JwtService(TEST_SECRET, 900);
        JwtService verifier = new JwtService(Base64.getEncoder().encodeToString(otherKey), 900);
        JwtAuthenticationFilter filter = filter(verifier, mock(UserDetailsService.class));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/doctors");
        request.setServletPath("/api/doctors");
        request.addHeader("Authorization", "Bearer " + issuer.generateAccessToken("patient@example.com"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> chainCalls.incrementAndGet());

        assertEquals(1, chainCalls.get());
        assertEquals(200, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void assistantEndpointIsPublicAndSkipsJwtProcessing() throws Exception {
        UserDetailsService users = mock(UserDetailsService.class);
        JwtAuthenticationFilter filter = filter(new JwtService(TEST_SECRET, 900), users);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/assistant/chat");
        request.setServletPath("/api/assistant/chat");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger();

        filter.doFilter(request, response,
                (servletRequest, servletResponse) -> chainCalls.incrementAndGet());

        assertEquals(1, chainCalls.get());
        verify(users, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void invalidSignatureIsRejectedAndDoesNotContinueChain() throws Exception {
        byte[] otherKey = new byte[32];
        otherKey[0] = 1;
        JwtService issuer = new JwtService(TEST_SECRET, 900);
        JwtService verifier = new JwtService(Base64.getEncoder().encodeToString(otherKey), 900);
        UserDetailsService users = mock(UserDetailsService.class);
        JwtAuthenticationFilter filter = filter(verifier, users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + issuer.generateAccessToken("patient@example.com"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger();

        filter.doFilterInternal(request, response, (servletRequest, servletResponse) -> chainCalls.incrementAndGet());

        assertEquals(401, response.getStatus());
        assertEquals(0, chainCalls.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(users, never()).loadUserByUsername("patient@example.com");
    }

    private JwtAuthenticationFilter filter(JwtService jwtService, UserDetailsService users) {
        AuthenticationEntryPoint entryPoint = (request, response, exception) -> {
            response.setStatus(401);
            response.getWriter().write("unauthorized");
        };
        return new JwtAuthenticationFilter(jwtService, users, entryPoint);
    }
}
