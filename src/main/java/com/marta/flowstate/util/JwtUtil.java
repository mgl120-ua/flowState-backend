package com.marta.flowstate.util;

import com.marta.flowstate.model.AppUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final String secretKey;

    public JwtUtil(@org.springframework.beans.factory.annotation.Value("${jwt.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String email) {
        if (email == null) return false;
        try {
            return extractUsername(token).equals(email);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String generateToken(AppUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("companyId", user.getCompany().getId());
        claims.put("role", user.getRol().getName());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) //10h
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenValid(String token, String email) {
        return extractUsername(token).equals(email);
    }
}
