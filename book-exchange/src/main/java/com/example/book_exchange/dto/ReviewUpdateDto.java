package com.example.book_exchange.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewUpdateDto {
    @NotBlank(message = "Token is required")
    private String token;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    private String comment;
}