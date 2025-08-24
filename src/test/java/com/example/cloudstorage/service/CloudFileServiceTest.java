package com.example.cloudstorage.service;

import com.example.cloudstorage.exception.FileNotFoundInStorageException;
import com.example.cloudstorage.exception.InvalidFilePathException;
import com.example.cloudstorage.model.CloudFile;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.CloudFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CloudFileServiceTest {

    @Mock
    private CloudFileRepository cloudFileRepository;

    @InjectMocks
    private CloudFileService cloudFileService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        cloudFileService.storageLocation = System.getProperty("java.io.tmpdir");
    }

    @Test
    void uploadFile_Success() throws IOException {
        User user = new User();
        user.setUsername("test");

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "file.txt", null, "content".getBytes());

        when(cloudFileRepository.findByOwnerAndFilename(user, "file.txt")).thenReturn(Optional.empty());
        when(cloudFileRepository.save(any(CloudFile.class))).thenAnswer(i -> i.getArgument(0));

        CloudFile uploaded = cloudFileService.uploadFile(user, multipartFile, "file.txt");

        assertEquals("file.txt", uploaded.getFilename());
        assertNotNull(uploaded.getFilepath());
        assertTrue(Files.exists(Path.of(uploaded.getFilepath())));

        // Clean up temp file
        Files.deleteIfExists(Path.of(uploaded.getFilepath()));
    }

    @Test
    void deleteFile_Success() throws IOException {
        User user = new User();
        user.setUsername("test");

        Path tempFile = Files.createTempFile("test", ".txt");
        CloudFile cloudFile = new CloudFile();
        cloudFile.setFilename("file.txt");
        cloudFile.setOwner(user);
        cloudFile.setFilepath(tempFile.toString());

        when(cloudFileRepository.findByOwnerAndFilename(user, "file.txt")).thenReturn(Optional.of(cloudFile));
        doNothing().when(cloudFileRepository).delete(cloudFile);

        cloudFileService.deleteFile(user, "file.txt");

        assertFalse(Files.exists(tempFile));
    }

    @Test
    void deleteFile_FileNotFound() {
        User user = new User();
        when(cloudFileRepository.findByOwnerAndFilename(user, "file.txt")).thenReturn(Optional.empty());

        assertThrows(FileNotFoundInStorageException.class, () ->
                cloudFileService.deleteFile(user, "file.txt"));
    }

    @Test
    void downloadFile_Success() throws IOException {
        User user = new User();
        user.setUsername("test");

        Path tempFile = Files.createTempFile("download", ".txt");
        Files.writeString(tempFile, "hello");

        CloudFile cloudFile = new CloudFile();
        cloudFile.setFilename("file.txt");
        cloudFile.setOwner(user);
        cloudFile.setFilepath(tempFile.toString());

        when(cloudFileRepository.findByOwnerAndFilename(user, "file.txt")).thenReturn(Optional.of(cloudFile));

        byte[] data = cloudFileService.downloadFile(user, "file.txt");

        assertEquals("hello", new String(data));

        Files.deleteIfExists(tempFile);
    }

    @Test
    void downloadFile_FileNotFound() {
        User user = new User();
        when(cloudFileRepository.findByOwnerAndFilename(user, "file.txt")).thenReturn(Optional.empty());

        assertThrows(FileNotFoundInStorageException.class, () ->
                cloudFileService.downloadFile(user, "file.txt"));
    }

    @Test
    void renameFile_Success() throws Exception {
        User user = new User();
        user.setUsername("test");

        Path tempFile = Files.createTempFile("old", ".txt");
        CloudFile cloudFile = new CloudFile();
        cloudFile.setFilename("old.txt");
        cloudFile.setOwner(user);
        cloudFile.setFilepath(tempFile.toString());

        when(cloudFileRepository.findByOwnerAndFilename(user, "old.txt")).thenReturn(Optional.of(cloudFile));
        when(cloudFileRepository.save(any(CloudFile.class))).thenAnswer(i -> i.getArgument(0));

        CloudFile renamed = cloudFileService.renameFile(user, "old.txt", "new.txt");

        assertEquals("new.txt", renamed.getFilename());
        assertTrue(Files.exists(Path.of(renamed.getFilepath())));

        Files.deleteIfExists(Path.of(renamed.getFilepath()));
    }

    @Test
    void renameFile_FileNotFound() {
        User user = new User();
        when(cloudFileRepository.findByOwnerAndFilename(user, "old.txt")).thenReturn(Optional.empty());

        assertThrows(FileNotFoundInStorageException.class, () ->
                cloudFileService.renameFile(user, "old.txt", "new.txt"));
    }

    @Test
    void renameFile_InvalidOldFilename() {
        User user = new User();
        assertThrows(InvalidFilePathException.class, () ->
                cloudFileService.renameFile(user, " ", "new.txt"));
    }

    @Test
    void renameFile_InvalidNewFilename() {
        User user = new User();
        assertThrows(InvalidFilePathException.class, () ->
                cloudFileService.renameFile(user, "old.txt", " "));
    }
}
