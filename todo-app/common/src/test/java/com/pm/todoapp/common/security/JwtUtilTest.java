package com.pm.todoapp.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.UUID;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private Key secretKey;

    @BeforeEach
    public void setUp(){

        String secretString = "VGhpcy1pcy1teS1zZWN1cmUta2V5LTI1Ni1iaXRzLWxvbmc=";
        this.jwtUtil = new JwtUtil(secretString);

        byte[] keyBytes = Base64.getDecoder().decode(secretString.getBytes(StandardCharsets.UTF_8));
        this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Test
    @DisplayName("Should generate valid and not empty token for given id")
    public void shouldGenerateValidAndEmptyTokenForGivenId(){
        String userId = UUID.randomUUID().toString();
        String token = this.jwtUtil.generateToken(userId);

        assertNotNull(token, "Generated token should not be null");
        assertFalse(token.isEmpty(), "Generated token should not be empty");

        Claims claims = Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(userId, claims.get("id", String.class), "Generated token does not match");
    }
}
