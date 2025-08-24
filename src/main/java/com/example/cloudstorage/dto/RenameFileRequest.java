package com.example.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RenameFileRequest {
    @JsonProperty("filename")
    private String name;
}
