package com.example.cloudstorage;

import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class TestUserInitializer {

    @Bean
    public CommandLineRunner initTestUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("test@example.com").isEmpty()) {
                User user = new User();
                user.setUsername("testuser"); // <-- ОБЯЗАТЕЛЬНО
                user.setEmail("test@example.com");
                user.setPassword(passwordEncoder.encode("test123"));
                userRepository.save(user);
                System.out.println("Создан тестовый пользователь: test@example.com / test123");
            }
        };
    }
}
