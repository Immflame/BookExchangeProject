package com.example.book_exchange.dto;

import com.example.book_exchange.model.enums.Genre;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookDto {
    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    private String description;
    private Long locationId;

    private Genre genre;
}
