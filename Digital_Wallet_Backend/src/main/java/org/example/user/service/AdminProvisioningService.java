package org.example.user.service;

import org.example.user.entity.Role;
import org.example.user.entity.User;
import org.example.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AdminProvisioningService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminProvisioningService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User promoteToAdmin(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        user.setRole(Role.ADMIN);

        return userRepository.save(user);
    }

    @Transactional
    public void provisionConfiguredAdmin(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalStateException(
                    "ADMIN_EMAIL must be configured for admin provisioning"
            );
        }

        String normalizedEmail = email.trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Configured admin user does not exist. Register the account first."
                ));

        user.setRole(Role.ADMIN);
        userRepository.save(user);
    }
}