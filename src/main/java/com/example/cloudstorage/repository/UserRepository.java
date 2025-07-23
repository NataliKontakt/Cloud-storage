package com.example.cloudstorage.repository;

import com.example.cloudstorage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Найти пользователя по логину (для логина)
    Optional<User> findByUsername(String username);

    // Найти пользователя по email (например, для проверки уникальности)
    Optional<User> findByEmail(String email);

    // Можно даже сделать поиск и по логину, и по email (для авторизации)
    Optional<User> findByUsernameOrEmail(String username, String email);
}
