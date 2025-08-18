package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.CloudFileDto;
import com.example.cloudstorage.model.CloudFile;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.CloudFileRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.StandardCopyOption;

@Service
public class CloudFileService {

    private final CloudFileRepository cloudFileRepository;

    @Value("${storage.location}") // путь берём из application.properties
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

        // сохраняем файл на диск
        Files.copy(file.getInputStream(), Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);

        // сохраняем запись в БД
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
}