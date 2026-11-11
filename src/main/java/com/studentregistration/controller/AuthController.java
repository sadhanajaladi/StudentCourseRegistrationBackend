package com.studentregistration.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentregistration.dto.AuthResponse;
import com.studentregistration.dto.LoginRequest;
import com.studentregistration.dto.SignupRequest;
import com.studentregistration.security.JwtService;
import com.studentregistration.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("[AuthController] Registration attempt for email: {}", request.getEmail());
        AuthResponse response = authService.signup(request);
        log.info("[AuthController] Registration successful for user ID: {}, email: {}", response.getId(), response.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("[AuthController] Login attempt for email: {}", request.getEmail());
        try {
            // 1. Authenticate with email & password
            Authentication authentication = authService.login(request.getEmail(), request.getPassword());

            // 2. Build user AuthResponse
            AuthResponse authResponse = authService.getAuthResponse(authentication, "Login successful");

            // 3. Generate JWT token and attach to response
            String token = jwtService.generateToken(authentication.getName(), authResponse.getRole());
            authResponse.setToken(token);

            log.info("[AuthController] Login successful for user: {} with role: {}", authResponse.getEmail(), authResponse.getRole());
            return ResponseEntity.ok(authResponse);

        } catch (AuthenticationException ex) {
            log.warn("[AuthController] Login failed for email: {} - Invalid credentials", request.getEmail());
            AuthResponse errorResponse = new AuthResponse(null, null, null, null, "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            log.warn("[AuthController] Unauthenticated request to /api/auth/me");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.debug("[AuthController] Current user info requested for: {}", authentication.getName());
        AuthResponse response = authService.getAuthResponse(authentication, "Current user retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) {
        String user = (authentication != null) ? authentication.getName() : "anonymous";
        log.info("[AuthController] User logged out: {}", user);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}