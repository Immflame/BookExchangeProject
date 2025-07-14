package com.example.book_exchange.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExchangeRequestDto {
    @NotNull(message = "Book1 ID is required")
    private Long book1Id;

    @NotNull(message = "Book2 ID is required")
    private Long book2Id;
}