package org.example.authentication.controller;

import jakarta.validation.Valid;
import org.example.authentication.dto.RegisterRequest;
import org.example.authentication.dto.RegisterResponse;
import org.example.user.entity.User;
import org.example.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.example.authentication.dto.LoginRequest;
import org.example.authentication.dto.LoginResponse;
import org.example.authentication.service.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService){
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request){
        User user = userService.registerUser(
                request.getName(), request.getEmail(), request.getPassword()
        );

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        User user = userService.authenticateUser(
                request.getEmail(),
                request.getPassword()
        );

        String token = jwtService.generateToken(user.getEmail());

        LoginResponse response = new LoginResponse(token, 900);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(
                "Authenticated as: " + authentication.getName()
        );
    }
}
