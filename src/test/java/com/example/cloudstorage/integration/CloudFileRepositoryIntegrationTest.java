package com.example.cloudstorage.integration;

import com.example.cloudstorage.model.CloudFile;
import com.example.cloudstorage.model.Role;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.CloudFileRepository;
import com.example.cloudstorage.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CloudFileRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CloudFileRepository cloudFileRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindFile() {
        // Создаём пользователя
        User user = new User();
        user.setUsername("fileowner");
        user.setEmail("owner@example.com");
        user.setPassword("pass");
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

        // Создаём файл
        CloudFile file = new CloudFile();
        file.setFilename("doc.txt");
        file.setFilepath("/tmp/doc.txt");
        file.setOwner(user);
        file.setUploadedAt(LocalDateTime.now());
        cloudFileRepository.save(file);

        // Проверяем сохранение
        List<CloudFile> files = cloudFileRepository.findTopN(10);
        assertThat(files).isNotEmpty();
        assertThat(files.get(0).getFilename()).isEqualTo("doc.txt");
        assertThat(files.get(0).getOwner().getEmail()).isEqualTo("owner@example.com");
    }
}
