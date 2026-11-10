
package com.studentregistration.controller;

import java.util.Map;

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

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(
            AuthService authService,
            JwtService jwtService) {

        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody SignupRequest request) {

        AuthResponse response = authService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        try {
            Authentication authentication = authService.login(
                    request.getEmail(),
                    request.getPassword()
            );

            AuthResponse authResponse =
                    authService.getAuthResponse(
                            authentication,
                            "Login successful"
                    );

            String token = jwtService.generateToken(
                    authentication.getName(),
                    authResponse.getRole()
            );

            authResponse.setToken(token);

            return ResponseEntity.ok(authResponse);

        } catch (AuthenticationException e) {

            AuthResponse errorResponse = new AuthResponse(
                    null,
                    null,
                    null,
                    null,
                    "Invalid email or password"
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(
            Authentication authentication) {

        AuthResponse response = authService.getAuthResponse(
                authentication,
                "Current user retrieved successfully"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Logged out successfully. Please discard your JWT token."
                )
        );
    }
}
