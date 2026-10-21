package com.example.smart_hospital_backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.jsonwebtoken.JwtException;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(new byte[32]);

    @Test
    void generatesAndVerifiesTokenSubject() {
        JwtService jwtService = new JwtService(TEST_SECRET, 900);
        String token = jwtService.generateAccessToken("patient@example.com");

        assertEquals("patient@example.com", jwtService.extractSubject(token));
    }

    @Test
    void rejectsTokenSignedWithDifferentKey() {
        byte[] differentKey = new byte[32];
        differentKey[0] = 1;
        JwtService issuer = new JwtService(TEST_SECRET, 900);
        JwtService verifier = new JwtService(
                Base64.getEncoder().encodeToString(differentKey), 900);
        String token = issuer.generateAccessToken("patient@example.com");

        assertThrows(JwtException.class, () -> verifier.extractSubject(token));
    }

    @Test
    void rejectsExpiredToken() throws InterruptedException {
        JwtService jwtService = new JwtService(TEST_SECRET, 1);
        String token = jwtService.generateAccessToken("patient@example.com");
        Thread.sleep(1_100);

        assertThrows(JwtException.class, () -> jwtService.extractSubject(token));
    }

    @Test
    void rejectsMissingOrWeakSigningSecrets() {
        assertThrows(IllegalStateException.class, () -> new JwtService("", 900));
        assertThrows(IllegalStateException.class, () -> new JwtService("not-base64!", 900));
        assertThrows(
                IllegalStateException.class,
                () -> new JwtService(Base64.getEncoder().encodeToString(new byte[16]), 900));
    }
}
