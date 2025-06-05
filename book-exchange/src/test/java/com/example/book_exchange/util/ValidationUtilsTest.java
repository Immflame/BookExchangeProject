package com.example.book_exchange.util;

import com.example.book_exchange.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void validateId_ShouldThrowForInvalidId() {
        assertThrows(InvalidRequestException.class,
                () -> ValidationUtils.validateId(null, "test"));

        assertThrows(InvalidRequestException.class,
                () -> ValidationUtils.validateId(-1L, "test"));

        assertThrows(InvalidRequestException.class,
                () -> ValidationUtils.validateId(0L, "test"));
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 100, Long.MAX_VALUE})
    void validateId_ShouldPassForValidId(Long id) {
        assertDoesNotThrow(() -> ValidationUtils.validateId(id, "test"));
    }
}