package com.example.cloudstorage.controller;

import com.example.cloudstorage.dto.CloudFileDto;
import com.example.cloudstorage.service.AuthService;
import com.example.cloudstorage.service.CloudFileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
// загрузка файла
    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename,
            @RequestParam("file") MultipartFile file) {
        try {
            var user = authService.getUserByToken(authToken); // Проверка токена
            cloudFileService.saveFile(user, filename, file);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Upload failed"));
        }
    }

}
