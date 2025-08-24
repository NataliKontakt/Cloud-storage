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

        var user = authService.getUserByToken(authToken);

        List<CloudFileDto> files = cloudFileService.getFiles(limit).stream()
                .map(cloudFileMapper::toDto)
                .toList();

        return ResponseEntity.ok(files);
    }

    @PostMapping("/file")
    public ResponseEntity<CloudFileDto> uploadFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename,
            @RequestParam("file") MultipartFile file) {

        var user = authService.getUserByToken(authToken);
        CloudFile uploadedFile = cloudFileService.uploadFile(user, file, filename);
        return ResponseEntity.ok(cloudFileMapper.toDto(uploadedFile));
    }

    @DeleteMapping("/file")
    public ResponseEntity<Map<String, String>> deleteFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename) {

        var user = authService.getUserByToken(authToken);
        cloudFileService.deleteFile(user, filename);
        return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
    }

    @GetMapping("/file")
    public ResponseEntity<byte[]> downloadFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename) {

        var user = authService.getUserByToken(authToken);
        byte[] fileBytes = cloudFileService.downloadFile(user, filename);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(fileBytes);
    }

    @PutMapping("/file")
    public ResponseEntity<CloudFileDto> renameFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String oldFilename,
            @RequestBody RenameFileRequest request) {

        var user = authService.getUserByToken(authToken);
        CloudFile renamedFile = cloudFileService.renameFile(user, oldFilename, request.getName());
        return ResponseEntity.ok(cloudFileMapper.toDto(renamedFile));
    }
}
