package com.example.cloudstorage.dto;

public class CloudFileDto {
    private String filename;
    private long size; // Размер файла в байтах

    public CloudFileDto() {}

    public CloudFileDto(String filename, long size) {
        this.filename = filename;
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}