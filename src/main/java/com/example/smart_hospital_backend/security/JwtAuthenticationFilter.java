package com.example.smart_hospital_backend.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();
        return ("GET".equals(method) && ("/api/health".equals(path)
                        || "/api/doctors".equals(path)
                        || "/api/departments".equals(path)
                        || path.startsWith("/api/departments/")))
                || ("POST".equals(method) && "/api/assistant/chat".equals(path))
                || ("POST".equals(method) && ("/api/auth/login".equals(path)
                        || "/api/auth/register".equals(path)
                        || "/api/patients".equals(path)));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            reject(request, response, new IllegalArgumentException("Bearer token is empty"));
            return;
        }

        try {
            String subject = jwtService.extractSubject(token);
            if (subject == null || subject.isBlank()) {
                throw new IllegalArgumentException("Token subject is missing");
            }

            UserDetails user = userDetailsService.loadUserByUsername(subject.trim().toLowerCase(Locale.ROOT));
            var authentication = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException exception) {
            SecurityContextHolder.clearContext();
            reject(request, response, exception);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void reject(
            HttpServletRequest request, HttpServletResponse response, Exception cause)
            throws IOException, ServletException {
        authenticationEntryPoint.commence(
                request,
                response,
                new BadCredentialsException("Invalid or expired access token", cause));
    }
}
