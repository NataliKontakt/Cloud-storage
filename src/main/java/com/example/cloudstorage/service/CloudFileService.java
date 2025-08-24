package com.example.cloudstorage.service;

import com.example.cloudstorage.exception.FileAlreadyExistsException;
import com.example.cloudstorage.exception.FileNotFoundInStorageException;
import com.example.cloudstorage.exception.FileStorageException;
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

    public CloudFile uploadFile(User user, MultipartFile multipartFile, String filename) {
        Path filePath = null;
        try {
            // Проверка на уникальность
            if (cloudFileRepository.findByOwnerAndFilename(user, filename).isPresent()) {
                throw new FileAlreadyExistsException("File with this name already exists");
            }

            // Убедимся, что папка для хранения существует
            Path storagePath = Paths.get(storageLocation);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            // Генерируем уникальное имя для хранения
            filePath = storagePath.resolve(UUID.randomUUID() + "_" + filename);

            // Записываем файл в файловое хранилище
            Files.write(filePath, multipartFile.getBytes());

            // Создаём сущность
            CloudFile file = new CloudFile();
            file.setFilename(filename);
            file.setFilepath(filePath.toString());
            file.setOwner(user);
            file.setUploadedAt(LocalDateTime.now());

            // Сохраняем в БД
            return cloudFileRepository.save(file);

        } catch (Exception e) {
            // Компенсация: если файл был записан, но БД не сохранилась
            if (filePath != null) {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException ex) {
                    // логируем, но не прерываем исключение
                    System.err.println("Rollback failed, could not delete file: " + filePath);
                }
            }
            throw new FileStorageException("File upload failed", e); // пробрасываем исключение
        }
    }


    @Transactional
    public void deleteFile(User user, String filename) {
        CloudFile file = cloudFileRepository.findByOwnerAndFilename(user, filename)
                .orElseThrow(() -> new FileNotFoundInStorageException("File not found"));

        Path path = Paths.get(file.getFilepath());

        try {
            // 1. Удаляем запись в БД
            cloudFileRepository.delete(file);

            // 2. Удаляем файл в ФС
            Files.deleteIfExists(path);

        } catch (Exception e) {
            // Компенсация: если БД удалилась, но файл в ФС не смог удалиться
            if (!cloudFileRepository.findByOwnerAndFilename(user, filename).isPresent()) {
                // возвращаем запись в БД (rollback)
                cloudFileRepository.save(file);
            }
            throw new FileStorageException("Delete failed: " + e.getMessage(), e);
        }
    }

    public byte[] downloadFile(User user, String filename) {
        CloudFile file = cloudFileRepository.findByOwnerAndFilename(user, filename)
                .orElseThrow(() -> new FileNotFoundInStorageException("File not found"));

        try {
            return Files.readAllBytes(Paths.get(file.getFilepath()));
        } catch (IOException e) {
            throw new FileStorageException("Failed to read file from storage", e);
        }
    }

    @Transactional
    public CloudFile renameFile(User user, String oldFilename, String newFilename) {
        CloudFile file = cloudFileRepository.findByOwnerAndFilename(user, oldFilename)
                .orElseThrow(() -> new FileNotFoundInStorageException("File not found"));

        Path oldPath = Paths.get(file.getFilepath());
        Path newPath = oldPath.resolveSibling(newFilename);

        try {
            Files.move(oldPath, newPath);
            file.setFilename(newFilename);
            file.setFilepath(newPath.toString());
            return cloudFileRepository.save(file);
        } catch (IOException e) {
            throw new FileStorageException("File rename failed: " + e.getMessage(), e);
        }
    }
}