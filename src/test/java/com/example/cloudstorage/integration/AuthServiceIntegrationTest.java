package com.example.cloudstorage.integration;

import com.example.cloudstorage.dto.LoginRequest;
import com.example.cloudstorage.exception.InvalidPasswordException;
import com.example.cloudstorage.exception.UnauthorizedException;
import com.example.cloudstorage.model.Role;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.UserRepository;
import com.example.cloudstorage.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user = new User();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword(passwordEncoder.encode("secret123"));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setLogin("john@example.com");
        request.setPassword("secret123");

        User loggedIn = authService.login(request);

        assertThat(loggedIn.getToken()).isNotNull();
        assertThat(loggedIn.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testLoginInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setLogin("john@example.com");
        request.setPassword("wrongpass");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void testGetUserByToken() {
        LoginRequest request = new LoginRequest();
        request.setLogin("john@example.com");
        request.setPassword("secret123");

        User loggedIn = authService.login(request);

        User fromToken = authService.getUserByToken("Bearer " + loggedIn.getToken());

        assertThat(fromToken.getUsername()).isEqualTo("john");
    }

    @Test
    void testLogoutAndAccess() {
        LoginRequest request = new LoginRequest();
        request.setLogin("john@example.com");
        request.setPassword("secret123");

        User loggedIn = authService.login(request);
        String token = loggedIn.getToken();

        authService.logout("Bearer " + token);

        assertThatThrownBy(() -> authService.getUserByToken("Bearer " + token))
                .isInstanceOf(UnauthorizedException.class);
    }
}
