
package com.studentregistration.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.studentregistration.entity.Role;
import com.studentregistration.entity.User;
import com.studentregistration.repository.UserRepository;

@Configuration
public class AdminInitializer {

    @Bean
    public ApplicationRunner createAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword) {

        return args -> {

            if (adminEmail.isBlank() || adminPassword.isBlank()) {
                System.out.println(
                        "Admin account not created: configure ADMIN_EMAIL and ADMIN_PASSWORD.");
                return;
            }

            String email = adminEmail.trim().toLowerCase(Locale.ROOT);

            if (userRepository.existsByEmail(email)) {
                System.out.println("Admin email already exists.");
                return;
            }

            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);

            System.out.println("Admin account created successfully.");
        };
    }
}
