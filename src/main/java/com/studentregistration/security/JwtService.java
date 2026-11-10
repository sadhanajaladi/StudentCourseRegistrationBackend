
package com.studentregistration.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret is not configured.");
        }

        this.signingKey =
                Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

        this.expirationMs = expirationMs;
    }

    // Generate a JWT token after successful login
    public String generateToken(String email, String role) {

        Date now = new Date();
        Date expiration = new Date(
                now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    // Extract the user's email from the token
    public String extractUsername(String token) {

        return extractAllClaims(token).getSubject();
    }

    // Extract all claims after verifying the signature
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Check whether the token belongs to this user and is valid
    public boolean isTokenValid(
            String token, UserDetails userDetails) {

        try {
            String username = extractUsername(token);

            return username.equals(userDetails.getUsername())
                    && extractAllClaims(token)
                            .getExpiration()
                            .after(new Date());

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
