package com.example.book_exchange.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LocationRequestDto {
    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}