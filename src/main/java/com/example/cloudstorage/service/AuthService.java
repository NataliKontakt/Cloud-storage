package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.LoginRequest;
import com.example.cloudstorage.exception.InvalidPasswordException;
import com.example.cloudstorage.exception.UnauthorizedException;
import com.example.cloudstorage.exception.UserNotFoundException;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User login(LoginRequest request) {
        log.info("Попытка входа: {}", request.getLogin());

        User user = userRepository
                .findByEmail(request.getLogin())
                .orElseThrow(() -> {
                    log.warn("Пользователь {} не найден", request.getLogin());
                    return new UserNotFoundException("User not found");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Неверный пароль для {}", request.getLogin());
            throw new InvalidPasswordException("Invalid password");
        }

        String token = UUID.randomUUID().toString();
        user.setToken(token);
        userRepository.save(user);

        log.info("Успешный вход: {}", user.getUsername());
        return user;
    }

    public void logout(String token) {
        log.info("Выход по токену {}", token);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Optional<User> userOpt = userRepository.findByToken(token);
        if (userOpt.isEmpty()) {
            log.warn("Невалидный токен {}", token);
            throw new UnauthorizedException("Invalid token: " + token);
        }

        User user;
        user = userOpt.get();
        user.setToken(null);
        userRepository.save(user);

        log.info("Пользователь {} вышел из системы", user.getUsername());
    }

    public User getUserByToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Optional<User> userOpt = userRepository.findByToken(token);
        if (userOpt.isEmpty()) {
            log.warn("Попытка доступа с невалидным токеном {}", token);
            throw new UnauthorizedException("Invalid token: " + token);
        }

        return userOpt.get();
    }

}
