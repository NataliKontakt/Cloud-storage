package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.LoginRequest;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authService = new AuthService(userRepository);
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setLogin("user1");
        request.setPassword("pass1");

        User user = new User();
        user.setUsername("user1");
        user.setPassword("pass1");

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        String token = authService.login(request);

        assertNotNull(token);
        assertDoesNotThrow(() -> UUID.fromString(token)); // проверяем, что токен валидный UUID
        assertEquals(token, user.getToken());

        // Проверяем, что userRepository.save был вызван с обновлённым пользователем
        verify(userRepository).save(user);
    }

    @Test
    void login_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setLogin("unknown");
        request.setPassword("pass");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void login_InvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setLogin("user1");
        request.setPassword("wrongpass");

        User user = new User();
        user.setUsername("user1");
        user.setPassword("correctpass");

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Invalid password", ex.getMessage());
    }

    @Test
    void logout_Success() {
        String token = UUID.randomUUID().toString();
        User user = new User();
        user.setToken(token);

        when(userRepository.findByToken(token)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        assertDoesNotThrow(() -> authService.logout(token));
        assertNull(user.getToken());

        verify(userRepository).save(user);
    }

    @Test
    void logout_InvalidToken() {
        String token = "invalid-token";

        when(userRepository.findByToken(token)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.logout(token));
        assertEquals("Invalid token", ex.getMessage());
    }
}
