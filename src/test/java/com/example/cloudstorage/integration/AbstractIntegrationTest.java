package com.example.cloudstorage.integration;

import org.springframework.boot.test.context.SpringBootTest;
import java.io.*;
import java.nio.file.*;

@SpringBootTest
public abstract class AbstractIntegrationTest {
    static {
        try {
            Path path = Paths.get(System.getProperty("user.home"), ".testcontainers.properties");
            if (!Files.exists(path)) {
                Files.writeString(path, "testcontainers.reuse.enable=true\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать ~/.testcontainers.properties", e);
        }
    }
}