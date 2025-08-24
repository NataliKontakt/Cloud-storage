package com.example.cloudstorage.service;

import com.example.cloudstorage.exception.*;
import com.example.cloudstorage.model.CloudFile;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.CloudFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.cloudstorage.exception.InvalidFilePathException;
import com.example.cloudstorage.exception.FileAlreadyExistsException;
import com.example.cloudstorage.exception.FileRenameException;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class CloudFileService {

    private final CloudFileRepository cloudFileRepository;

    @Value("${storage.location}")
    String storageLocation;

    public CloudFileService(CloudFileRepository cloudFileRepository) {
        this.cloudFileRepository = cloudFileRepository;
    }

    public List<CloudFile> getFiles(int limit) {
        log.debug("Получен список файлов (limit = {})", limit);
        return cloudFileRepository.findTopN(limit);
    }

    public CloudFile uploadFile(User user, MultipartFile multipartFile, String filename) {
        Path filePath = null;
        try {
            if (cloudFileRepository.findByOwnerAndFilename(user, filename).isPresent()) {
                log.warn("Файл {} уже существует у пользователя {}", filename, user.getUsername());
                throw new FileAlreadyExistsException("File with this name already exists");
            }

            Path storagePath = Paths.get(storageLocation);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
                log.debug("Создана папка {}", storagePath);
            }

            filePath = storagePath.resolve(UUID.randomUUID() + "_" + filename);
            Files.write(filePath, multipartFile.getBytes());

            CloudFile file = new CloudFile();
            file.setFilename(filename);
            file.setFilepath(filePath.toString());
            file.setOwner(user);
            file.setUploadedAt(LocalDateTime.now());

            CloudFile savedFile = cloudFileRepository.save(file);
            log.info("Файл {} загружен пользователем {} (путь: {})", filename, user.getUsername(), filePath);
            return savedFile;

        } catch (Exception e) {
            if (filePath != null) {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException ex) {
                    log.error("Не удалось удалить файл при rollback: {}", filePath, ex);
                }
            }
            log.error("Ошибка загрузки файла {} пользователем {}", filename, user.getUsername(), e);
            throw new FileStorageException("File upload failed", e);
        }
    }

    @Transactional
    public void deleteFile(User user, String filename) {
        CloudFile file = cloudFileRepository.findByOwnerAndFilename(user, filename)
                .orElseThrow(() -> {
                    log.warn("Файл {} не найден у пользователя {}", filename, user.getUsername());
                    return new FileNotFoundInStorageException("File not found");
                });

        Path path = Paths.get(file.getFilepath());

        try {
            cloudFileRepository.delete(file);
            Files.deleteIfExists(path);
            log.info("Файл {} удалён у пользователя {}", filename, user.getUsername());
        } catch (Exception e) {
            log.error("Ошибка удаления файла {} у пользователя {}", filename, user.getUsername(), e);
            cloudFileRepository.save(file);
            throw new FileStorageException("Delete failed: " + e.getMessage(), e);
        }
    }

    public byte[] downloadFile(User user, String filename) {
        CloudFile file = cloudFileRepository.findByOwnerAndFilename(user, filename)
                .orElseThrow(() -> {
                    log.warn("Файл {} не найден у пользователя {}", filename, user.getUsername());
                    return new FileNotFoundInStorageException("File not found");
                });

        try {
            log.info("Пользователь {} скачивает файл {}", user.getUsername(), filename);
            return Files.readAllBytes(Paths.get(file.getFilepath()));
        } catch (IOException e) {
            log.error("Ошибка чтения файла {} у пользователя {}", filename, user.getUsername(), e);
            throw new FileStorageException("Failed to read file from storage", e);
        }
    }

    public CloudFile renameFile(User user, String oldFilename, String newFilename)
            throws InvalidFilePathException, FileRenameException {
        if (oldFilename == null || oldFilename.isBlank()) {
            throw new InvalidFilePathException("Old filename is null or empty");
        }
        if (newFilename == null || newFilename.isBlank()) {
            throw new InvalidFilePathException("New filename is null or empty");
        }

        // Ищем файл в базе
        CloudFile oldFile = cloudFileRepository
                .findByOwnerAndFilename(user, oldFilename)
                .orElseThrow(() -> new FileNotFoundInStorageException("File not found: " + oldFilename));

        // Проверяем, что filepath не null
        if (oldFile.getFilepath() == null || oldFile.getFilepath().isBlank()) {
            throw new InvalidFilePathException("Filepath is null or empty for file: " + oldFilename);
        }

        // Создаём новый путь на диске
        Path oldPath = Path.of(oldFile.getFilepath());
        Path newPath = oldPath.resolveSibling(newFilename);

        try {
            // Переименовываем файл на диске
            Files.move(oldPath, newPath);
        } catch (IOException e) {
            throw new FileRenameException("Failed to rename file " + oldFilename + " to " + newFilename, e);
        }

        // Обновляем запись в базе
        oldFile.setFilename(newFilename);
        oldFile.setFilepath(newPath.toString());
        CloudFile savedFile = cloudFileRepository.save(oldFile);

        log.info("File renamed: {} -> {} for user {}", oldFilename, newFilename, user.getUsername());
        return savedFile;
    }

}