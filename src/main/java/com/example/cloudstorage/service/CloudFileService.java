package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.CloudFileDto;
import com.example.cloudstorage.model.CloudFile;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.CloudFileRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.StandardCopyOption;

@Service
public class CloudFileService {

    private final CloudFileRepository cloudFileRepository;

    @Value("${storage.location}")
    private String storageLocation;

    public CloudFileService(CloudFileRepository cloudFileRepository) {
        this.cloudFileRepository = cloudFileRepository;
    }

    public void saveFile(User user, String filename, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        Files.createDirectories(Paths.get(storageLocation));

        String fullPath = Paths.get(storageLocation, user.getId() + "_" + filename).toString();
        Files.copy(file.getInputStream(), Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);

        CloudFile cloudFile = new CloudFile();
        cloudFile.setFilename(filename);
        cloudFile.setFilepath(fullPath);
        cloudFile.setOwner(user);

        cloudFileRepository.save(cloudFile);
    }

    public List<CloudFileDto> getFiles(int limit) {
        List<CloudFile> files = cloudFileRepository.findTopN(limit);
        return files.stream()
                .map(f -> new CloudFileDto(f.getFilename(), getFileSize(f.getFilepath())))
                .collect(Collectors.toList());
    }

    private long getFileSize(String filepath) {
        try {
            return Files.size(Paths.get(filepath));
        } catch (IOException e) {
            return 0;
        }
    }

    public void uploadFile(User user, MultipartFile multipartFile, String filename) throws IOException {
        if (cloudFileRepository.findByOwnerAndFilename(user, filename).isPresent()) {
            throw new IllegalArgumentException("File with this name already exists");
        }

        Path storagePath = Paths.get(storageLocation);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        Path filePath = storagePath.resolve(UUID.randomUUID() + "_" + filename);
        Files.write(filePath, multipartFile.getBytes());

        CloudFile file = new CloudFile();
        file.setFilename(filename);
        file.setFilepath(filePath.toString());
        file.setOwner(user);
        file.setUploadedAt(LocalDateTime.now());

        cloudFileRepository.save(file);
    }

    public void deleteFile(User user, String filename) throws IOException {
        CloudFile file = cloudFileRepository.findByOwnerAndFilename(user, filename)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        Files.deleteIfExists(Paths.get(file.getFilepath()));
        cloudFileRepository.delete(file);
    }

    public byte[] downloadFile(User user, String filename) throws IOException {
        CloudFile file = cloudFileRepository.findByOwnerAndFilename(user, filename)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        return Files.readAllBytes(Paths.get(file.getFilepath()));
    }

    @Transactional
    public void renameFile(User user, String oldFilename) {
        CloudFile file = cloudFileRepository.findByOwnerAndFilename(user, oldFilename)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        // Генерируем новое имя: oldFilename + "_renamed_" + timestamp + расширение
        String extension = "";
        int dotIndex = oldFilename.lastIndexOf('.');
        if (dotIndex != -1) {
            extension = oldFilename.substring(dotIndex);
        }
        String baseName = dotIndex != -1 ? oldFilename.substring(0, dotIndex) : oldFilename;
        String newFilename = baseName + "_renamed_" + System.currentTimeMillis() + extension;

        Path oldPath = Paths.get(file.getFilepath());
        Path newPath = oldPath.resolveSibling(newFilename);

        try {
            Files.move(oldPath, newPath);
            file.setFilename(newFilename);
            file.setFilepath(newPath.toString());
            cloudFileRepository.save(file);
        } catch (IOException e) {
            throw new RuntimeException("File rename failed: " + e.getMessage(), e);
        }
    }
}