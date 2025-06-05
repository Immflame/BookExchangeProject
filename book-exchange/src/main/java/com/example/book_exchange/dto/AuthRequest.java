package com.example.book_exchange.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRequest {
    @NotBlank(message= "Username is required")
    private String username;
    @NotBlank(message= "Password is required")
    private String password;
}