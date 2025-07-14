package com.example.book_exchange.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocationRequestDto {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}