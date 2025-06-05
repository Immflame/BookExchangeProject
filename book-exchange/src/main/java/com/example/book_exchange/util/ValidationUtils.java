package com.example.book_exchange.util;

import com.example.book_exchange.exception.InvalidRequestException;

public class ValidationUtils {
    public static void validateId(Long id, String entity) {
        if (id == null || id <= 0) {
            throw new InvalidRequestException("Invalid %s ID".formatted(entity));
        }
    }
}