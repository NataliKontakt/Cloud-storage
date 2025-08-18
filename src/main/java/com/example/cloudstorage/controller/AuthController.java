package com.example.cloudstorage.controller;

import com.example.cloudstorage.dto.LoginRequest;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        User user = authService.login(request);
        return ResponseEntity.ok(Map.of(
                "auth-token", user.getToken() // по спецификации
        ));
    }
    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> loginInfo(@RequestParam(required = false) String logout) {
        Map<String, String> response = new HashMap<>();
        if (logout != null) {
            response.put("message", "Вы успешно вышли из системы");
        } else {
            response.put("message", "Пожалуйста, авторизуйтесь");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("auth-token") String token) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }
}

