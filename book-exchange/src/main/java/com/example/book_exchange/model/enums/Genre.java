package com.example.book_exchange.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "Genre enumeration")
@Getter
public enum Genre {
    FANTASY("Fantasy"),
    SCIENCE_FICTION("Science Fiction"),
    DETECTIVE("Detective/Mystery/Thriller"),
    BIOGRAPHY("Biography/Autobiography"),
    HISTORY("History"),
    SCIENCE_TECHNOLOGY("Science and Technology"),
    SELF_IMPROVEMENT("Self-Improvement");

    private final String displayName;

    Genre(String displayName) {
        this.displayName = displayName;
    }

}

