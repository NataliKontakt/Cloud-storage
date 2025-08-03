package com.example.cloudstorage.security;

import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DbTokenService implements TokenService {

    private final UserRepository userRepository;

    public DbTokenService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean validateToken(String token) {
        return userRepository.findByToken(token).isPresent();
    }

    @Override
    public String getUsernameFromToken(String token) {
        return userRepository.findByToken(token)
                .map(User::getUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid token"));
    }
}
