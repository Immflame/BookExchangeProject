package com.example.book_exchange.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewCreateDto {
    @NotBlank
    private String token;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;

    @NotNull
    private Long revieweeId;
}