package com.example.book_exchange.dto;

import com.example.book_exchange.model.enums.ExchangeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ExchangeResponseDto {
    private Long id;
    private BookResponseDto book1;
    private BookResponseDto book2;
    private UserResponseDto user1;
    private UserResponseDto user2;
    private ExchangeStatus status;
    private LocalDateTime exchangeDate;
}