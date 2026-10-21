package com.example.smart_hospital_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtService(
            @Value("${app.jwt.secret:}") String base64Secret,
            @Value("${app.jwt.access-token-expiration-seconds:900}")
            long expirationSeconds) {

        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET must be configured as a Base64-encoded key containing at least 32 bytes");
        }

        if (expirationSeconds <= 0 || expirationSeconds > 3600) {
            throw new IllegalStateException(
                    "JWT access token expiration must be between 1 and 3600 seconds");
        }

        byte[] keyBytes;

        try {
            keyBytes = Decoders.BASE64.decode(base64Secret);
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "JWT_SECRET must be valid Base64",
                    exception);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must decode to at least 32 bytes for HS256");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);

        this.expirationSeconds = expirationSeconds;
    }

    public String generateAccessToken(String subject) {

        Instant now = Instant.now();

        Instant expiresAt = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public String extractSubject(String token) {

        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationSeconds() {

        return expirationSeconds;
    }
}
