package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.CloudFileDto;
import com.example.cloudstorage.model.CloudFile;
import com.example.cloudstorage.repository.CloudFileRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CloudFileService {

    private final CloudFileRepository cloudFileRepository;

    public CloudFileService(CloudFileRepository cloudFileRepository) {
        this.cloudFileRepository = cloudFileRepository;
    }

    public List<CloudFileDto> getFiles(int limit) {
        List<CloudFile> files = cloudFileRepository.findTopN(limit);
        // Маппинг из CloudFile -> CloudFileDto
        return files.stream()
                .map(f -> new CloudFileDto(f.getFilename(), getFileSize(f.getFilepath())))
                .collect(Collectors.toList());
    }

    private long getFileSize(String filepath) {
        // Можно получить размер файла на диске, например
        try {
            return Files.size(Paths.get(filepath));
        } catch (IOException e) {
            return 0;
        }
    }
}