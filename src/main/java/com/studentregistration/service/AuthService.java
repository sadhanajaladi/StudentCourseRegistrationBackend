
package com.studentregistration.service;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentregistration.dto.AuthResponse;
import com.studentregistration.dto.SignupRequest;
import com.studentregistration.entity.Role;
import com.studentregistration.entity.User;
import com.studentregistration.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists.");
        }

        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);

        User savedUser = userRepository.save(user);

        emailService.sendAccountCreatedEmail(
                savedUser.getEmail(),
                savedUser.getName());

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "Account created successfully");
    }

    public Authentication login(String email, String password) {

        String normalizedEmail = email.trim()
                .toLowerCase(Locale.ROOT);

        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        password));
    }

    public AuthResponse getAuthResponse(
            Authentication authentication,
            String message) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User account was not found."));

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                message);
    }

    public void sendLoginEmail(Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User account was not found."));

        emailService.sendLoginNotification(
                user.getEmail(),
                user.getName());
    }
}
