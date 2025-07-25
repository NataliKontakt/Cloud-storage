package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.LoginRequest;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Логин: проверка пользователя, генерация токена
     */
    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getLogin())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Генерируем токен (UUID)
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        userRepository.save(user);

        return token;
    }

    /**
     * Логаут: сброс токена
     */
    public void logout(String token) {
        User user = userRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        user.setToken(null);
        userRepository.save(user);
    }
}
