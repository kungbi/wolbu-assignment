package com.company.wolbu.assignment.auth.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.company.wolbu.assignment.auth.domain.MemberRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties properties;

    public String generateAccessToken(Long userId, String email, MemberRole role) {
        if (properties.getSecret() == null) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        Date now = new Date();
        Date exp = new Date(now.getTime() + properties.getAccessTtlSeconds() * 1000);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(exp)
                .addClaims(Map.of(
                        "email", email,
                        "role", role.name()
                ))
                .signWith(Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        if (properties.getSecret() == null) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        Date now = new Date();
        Date exp = new Date(now.getTime() + properties.getRefreshTtlSeconds() * 1000);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parse(String token) {
        if (properties.getSecret() == null) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}


