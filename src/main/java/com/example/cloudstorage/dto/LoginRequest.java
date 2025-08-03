package com.example.cloudstorage.dto;

import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.processing.Pattern;

@Data
public class LoginRequest {
    private String login;
    private String password;
    private String role;
}
