package com.example.cloudstorage.controller;

import com.example.cloudstorage.dto.LoginRequest;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
@CrossOrigin(origins = "http://localhost:8081")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        User user = authService.login(request);
        return ResponseEntity.ok(Map.of(
                "token", user.getToken(),
                "email", user.getEmail(),
                "username", user.getUsername()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }
}

