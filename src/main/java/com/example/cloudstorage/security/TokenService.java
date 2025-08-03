package com.example.cloudstorage.security;

public interface TokenService {
    boolean validateToken(String token);
    String getUsernameFromToken(String token);
}
