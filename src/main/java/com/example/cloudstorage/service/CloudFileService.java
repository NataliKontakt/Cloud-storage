package com.example.cloudstorage.service;

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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudFileService {

    private final CloudFileRepository cloudFileRepository;

    @Value("${storage.location}")
    private String storageLocation;

    public CloudFileService(CloudFileRepository cloudFileRepository) {
        this.cloudFileRepository = cloudFileRepository;
    }

    public List<CloudFile> getFiles(int limit) {
        return cloudFileRepository.findTopN(limit);
    }

    public CloudFile uploadFile(User user, MultipartFile multipartFile, String filename) throws IOException {
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

        return cloudFileRepository.save(file);
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
    public CloudFile renameFile(User user, String oldFilename, String newFilename) {
        CloudFile file = cloudFileRepository.findByOwnerAndFilename(user, oldFilename)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        Path oldPath = Paths.get(file.getFilepath());
        Path newPath = oldPath.resolveSibling(newFilename);

        try {
            Files.move(oldPath, newPath);
            file.setFilename(newFilename);
            file.setFilepath(newPath.toString());
            return cloudFileRepository.save(file);
        } catch (IOException e) {
            throw new RuntimeException("File rename failed: " + e.getMessage(), e);
        }
    }
}