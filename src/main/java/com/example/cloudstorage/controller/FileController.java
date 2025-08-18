package com.example.cloudstorage.controller;

import com.example.cloudstorage.dto.CloudFileDto;
import com.example.cloudstorage.dto.ErrorResponse;
import com.example.cloudstorage.service.AuthService;
import com.example.cloudstorage.service.CloudFileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

        authService.getUserByToken(authToken);
        List<CloudFileDto> files = cloudFileService.getFiles(limit);
        return ResponseEntity.ok(files);
    }

    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename,
            @RequestParam("file") MultipartFile file) {
        try {
            var user = authService.getUserByToken(authToken);
            cloudFileService.uploadFile(user, file, filename);
            return ResponseEntity.ok(Map.of("message", "File uploaded successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "id", 400));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "File upload failed", "id", 500));
        }
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename) {
        try {
            var user = authService.getUserByToken(authToken);
            cloudFileService.deleteFile(user, filename);
            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "id", 400));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Delete failed", "id", 500));
        }
    }

    @GetMapping("/file")
    public ResponseEntity<?> downloadFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename) {
        try {
            var user = authService.getUserByToken(authToken);
            byte[] fileBytes = cloudFileService.downloadFile(user, filename);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(fileBytes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "id", 400));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "File read error", "id", 500));
        }
    }

    @PutMapping("/file")
    public ResponseEntity<?> renameFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String oldFilename) {

        try {
            var user = authService.getUserByToken(authToken);
            cloudFileService.renameFile(user, oldFilename); // новое имя генерируется внутри сервиса
            return ResponseEntity.ok(Map.of("message", "File renamed successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage(), 400));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(500)
                    .body(new ErrorResponse(e.getMessage(), 500));

        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body(new ErrorResponse("Unexpected error", 500));
        }
    }
}
