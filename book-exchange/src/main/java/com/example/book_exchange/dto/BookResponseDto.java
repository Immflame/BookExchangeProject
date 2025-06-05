package com.example.book_exchange.dto;

import com.example.book_exchange.model.enums.BookStatus;
import com.example.book_exchange.model.enums.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponseDto {
    private Long id;
    private String title;
    private String author;
    private String description;
    private LocationResponseDto location;
    private Long userId;
    private String username;
    private Genre genre;
    private BookStatus status;
}