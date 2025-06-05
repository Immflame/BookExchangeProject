package com.example.book_exchange.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Book status enumeration")
public enum BookStatus {
    AVAILABLE,
    IN_EXCHANGE,
    EXCHANGED
}

