package com.example.book_exchange.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDto {
    @NotBlank
    private String token;

    @NotBlank
    private String username;

    private String password;
}