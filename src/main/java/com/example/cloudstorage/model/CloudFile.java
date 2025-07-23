package com.example.cloudstorage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "files")  // таблица в БД будет называться "files"
public class CloudFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;  // имя файла, которое видит пользователь

    @Column(nullable = false)
    private String filepath;  // путь на диске (storage.location + имя файла)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;  // владелец файла

    private LocalDateTime uploadedAt = LocalDateTime.now();
}
