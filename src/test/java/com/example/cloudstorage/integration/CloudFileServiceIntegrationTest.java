package com.example.cloudstorage.integration;

import com.example.cloudstorage.model.Role;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.UserRepository;
import com.example.cloudstorage.service.CloudFileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CloudFileServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CloudFileService cloudFileService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testUploadAndDownloadFile(@TempDir Path tempDir) throws Exception {
        // Создаём пользователя
        User user = new User();
        user.setUsername("uploader");
        user.setEmail("uploader@example.com");
        user.setPassword("pwd");
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

        // Подменяем storageLocation через reflection на временную папку
        Field storageField = CloudFileService.class.getDeclaredField("storageLocation");
        storageField.setAccessible(true);
        storageField.set(cloudFileService, tempDir.toString());

        // Создаём MockMultipartFile
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "hello.txt", "text/plain", "Hello World".getBytes()
        );

        // Загружаем файл
        var uploaded = cloudFileService.uploadFile(user, multipartFile, "hello.txt");
        assertThat(uploaded.getFilename()).isEqualTo("hello.txt");

        // Скачиваем файл
        byte[] content = cloudFileService.downloadFile(user, "hello.txt");
        assertThat(new String(content)).isEqualTo("Hello World");
    }
}
