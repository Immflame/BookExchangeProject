package com.example.book_exchange.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Builder
public class ReviewResponseDto {
    private Long id;
    private Long reviewerId;
    private String reviewerUsername;
    private Long revieweeId;
    private String revieweeUsername;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}