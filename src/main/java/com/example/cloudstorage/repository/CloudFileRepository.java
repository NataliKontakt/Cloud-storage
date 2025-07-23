package com.example.cloudstorage.repository;

import com.example.cloudstorage.model.CloudFile;
import com.example.cloudstorage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CloudFileRepository extends JpaRepository<CloudFile, Long> {
    List<CloudFile> findByOwner(User owner);
}
