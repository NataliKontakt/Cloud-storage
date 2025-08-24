package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.LoginRequest;
import com.example.cloudstorage.exception.InvalidPasswordException;
import com.example.cloudstorage.exception.UnauthorizedException;
import com.example.cloudstorage.exception.UserNotFoundException;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User login(LoginRequest request) {
        User user = userRepository
                .findByEmail(request.getLogin())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Invalid password");
        }

        // Генерация токена
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        userRepository.save(user);

        return user; // возвращаем пользователя с токеном
    }

    public void logout(String token) {
        // Убираем "Bearer " из заголовка, если он там есть
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        User user = userRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid token"));
        user.setToken(null);
        userRepository.save(user);
    }

    public User getUserByToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return userRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid token"));
    }
}
