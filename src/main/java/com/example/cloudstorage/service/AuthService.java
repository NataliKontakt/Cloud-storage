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

    public User login(LoginRequest request) {
        User user = userRepository
                .findByUsernameOrEmail(request.getLogin(), request.getLogin())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Генерация токена
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        userRepository.save(user);

        return user; // возвращаем пользователя с токеном
    }

    public void logout(String token) {
        // Убираем "Bearer " из заголовка, если он там есть
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        User user = userRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        user.setToken(null);
        userRepository.save(user);
    }
}
