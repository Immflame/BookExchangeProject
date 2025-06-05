package com.example.book_exchange.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExchangeRequestDto {
    @NotBlank(message = "Token is required")
    private String token;

    @NotNull(message = "Book1 ID is required")
    private Long book1Id;

    @NotNull(message = "Book2 ID is required")
    private Long book2Id;
}