package com.example.cloudstorage.repository;

import com.example.cloudstorage.model.CloudFile;
import com.example.cloudstorage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CloudFileRepository extends JpaRepository<CloudFile, Long> {

    @Query(value = "SELECT * FROM files LIMIT :limit", nativeQuery = true)
    List<CloudFile> findTopN(@Param("limit") int limit);
}
