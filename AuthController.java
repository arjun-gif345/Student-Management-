package com.example.student.management.controller;




import com.example.student.management.entity.AppUser;
import com.example.student.management.repository.UserRepository;
import com.example.student.management.service.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(
            UserRepository repository,
            PasswordEncoder encoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.repository = repository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody AppUser user) {

        if (user.getUsername() == null
                || user.getUsername().isBlank()
                || user.getPassword() == null
                || user.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Username and password are required"));
        }

        if (repository.existsByUsername(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message",
                            "Username already exists"));
        }

        user.setPassword(encoder.encode(user.getPassword()));
        repository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody AppUser user) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(), user.getPassword()));

        String token = jwtService.generateToken(
                user.getUsername());

        return ResponseEntity.ok(
                Map.of("token", token));
    }
}
