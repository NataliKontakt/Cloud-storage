package com.example.cloudstorage.controller;

import com.example.cloudstorage.dto.CloudFileDto;
import com.example.cloudstorage.dto.ErrorResponse;
import com.example.cloudstorage.dto.RenameFileRequest;
import com.example.cloudstorage.mapper.CloudFileMapper;
import com.example.cloudstorage.model.CloudFile;
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
    private final CloudFileMapper cloudFileMapper;

    public FileController(CloudFileService cloudFileService,
                          AuthService authService,
                          CloudFileMapper cloudFileMapper) {
        this.cloudFileService = cloudFileService;
        this.authService = authService;
        this.cloudFileMapper = cloudFileMapper;
    }

    @GetMapping("/list")
    public ResponseEntity<List<CloudFileDto>> listFiles(
            @RequestHeader("auth-token") String authToken,
            @RequestParam(defaultValue = "10") int limit) {

        authService.getUserByToken(authToken);

        List<CloudFileDto> files = cloudFileService.getFiles(limit).stream()
                .map(cloudFileMapper::toDto)
                .toList();

        return ResponseEntity.ok(files);
    }

    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename,
            @RequestParam("file") MultipartFile file) {
        try {
            var user = authService.getUserByToken(authToken);
            CloudFile uploadedFile = cloudFileService.uploadFile(user, file, filename);
            return ResponseEntity.ok(cloudFileMapper.toDto(uploadedFile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("File upload failed", 500));
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
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Delete failed", 500));
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
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), 400));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("File read error", 500));
        }
    }

    @PutMapping("/file")
    public ResponseEntity<?> renameFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String oldFilename,
            @RequestBody RenameFileRequest request) {

        try {
            var user = authService.getUserByToken(authToken);
            CloudFile renamedFile = cloudFileService.renameFile(user, oldFilename, request.getName());
            return ResponseEntity.ok(cloudFileMapper.toDto(renamedFile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), 400));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(new ErrorResponse(e.getMessage(), 500));
        }
    }
}
