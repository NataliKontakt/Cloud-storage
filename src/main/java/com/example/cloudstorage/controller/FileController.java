package com.example.cloudstorage.controller;

import com.example.cloudstorage.dto.CloudFileDto;
import com.example.cloudstorage.service.AuthService;
import com.example.cloudstorage.service.CloudFileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FileController {

    private final CloudFileService cloudFileService;
    private final AuthService authService;

    public FileController(CloudFileService cloudFileService, AuthService authService) {
        this.cloudFileService = cloudFileService;
        this.authService = authService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<CloudFileDto>> listFiles(
            @RequestHeader("auth-token") String authToken,
            @RequestParam(defaultValue = "10") int limit) {

        // Проверяем токен (бросит исключение, если не валиден)
        authService.getUserByToken(authToken);

        List<CloudFileDto> files = cloudFileService.getFiles(limit);

        return ResponseEntity.ok(files);
    }
}
