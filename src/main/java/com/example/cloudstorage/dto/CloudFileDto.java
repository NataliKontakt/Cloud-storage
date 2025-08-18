package com.example.cloudstorage.dto;

import lombok.Data;

@Data
public class CloudFileDto {
    private String filename;
    private long size; // Размер файла в байтах

    public CloudFileDto() {
    }

    public CloudFileDto(String filename, long size) {
        this.filename = filename;
        this.size = size;
    }

}