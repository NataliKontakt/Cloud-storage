package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.LoginRequest;
import com.example.cloudstorage.exception.InvalidPasswordException;
import com.example.cloudstorage.exception.UserNotFoundException;
import com.example.cloudstorage.exception.UnauthorizedException;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_Success() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("encoded");

        LoginRequest request = new LoginRequest();
        request.setLogin("email@example.com");
        request.setPassword("password");

        when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User loggedIn = authService.login(request);

        assertNotNull(loggedIn.getToken());
        verify(userRepository).save(user);
    }

    @Test
    void login_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setLogin("email@example.com");
        request.setPassword("password");

        when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(request));
    }

    @Test
    void login_InvalidPassword() {
        User user = new User();
        user.setPassword("encoded");

        LoginRequest request = new LoginRequest();
        request.setLogin("email@example.com");
        request.setPassword("wrong");

        when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> authService.login(request));
    }

    @Test
    void logout_Success() {
        String token = "Bearer validToken";
        User user = new User();
        user.setUsername("testUser");
        user.setToken("validToken");

        when(userRepository.findByToken("validToken")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        authService.logout(token);

        assertNull(user.getToken());
        verify(userRepository).save(user);
    }

    @Test
    void logout_InvalidToken() {
        when(userRepository.findByToken("token")).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class, () -> authService.logout("Bearer token"));
    }
}
