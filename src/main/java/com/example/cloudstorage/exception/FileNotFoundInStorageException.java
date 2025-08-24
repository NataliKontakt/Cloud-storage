package com.example.cloudstorage.exception;

public class FileNotFoundInStorageException extends RuntimeException {
    public FileNotFoundInStorageException(String message) {
        super(message);
    }
}
