package com.example.cloudstorage.repository;

import com.example.cloudstorage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // Вот этот метод — он ищет пользователя по username или по email
    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByToken(String token);
}
/*
public interface UserRepository extends JpaRepository<User, Long> {

    // Найти пользователя по логину (для логина)
    Optional<User> findByUsername(String username);

    // Найти пользователя по email (например, для проверки уникальности)
    Optional<User> findByEmail(String email);

    // Поиск либо по логину, либо по email (для авторизации)
    Optional<User> findByUsernameOrEmail(String username, String email);

    // Поиск по токену (для logout и фильтра авторизации)
    Optional<User> findByToken(String token);
}
*/
