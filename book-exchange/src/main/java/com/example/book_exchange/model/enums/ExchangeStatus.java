package com.example.book_exchange.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Exchange status enumeration")
public enum ExchangeStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}
