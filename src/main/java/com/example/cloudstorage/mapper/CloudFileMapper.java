package com.example.cloudstorage.mapper;

import com.example.cloudstorage.dto.CloudFileDto;
import com.example.cloudstorage.model.CloudFile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class CloudFileMapper {

    public CloudFileDto toDto(CloudFile file) {
        long size = 0;
        try {
            size = Files.size(Paths.get(file.getFilepath()));
        } catch (IOException e) {
            // оставляем 0, если не удалось прочитать
        }

        return new CloudFileDto(file.getFilename(), size);
    }
}
