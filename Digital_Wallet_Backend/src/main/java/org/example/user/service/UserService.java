package org.example.user.service;

import org.springframework.transaction.annotation.Transactional;
import org.example.user.entity.User;
import org.example.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Locale;
import java.util.Optional;
import org.example.wallet.service.WalletService;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;

    UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletService walletService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
    }

    @Transactional
    public User registerUser(String name, String email, String rawPassword){
        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)){
            throw new IllegalArgumentException("Email is already registered");
        }

        String passwordHash = passwordEncoder.encode(rawPassword);

        User user = new User(name.trim(), normalizedEmail, passwordHash);

        User savedUser = userRepository.save(user);
        walletService.createWallet(savedUser);
        return savedUser;
    }

    public User authenticateUser(String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return user;
    }

}
