package com.example.book_exchange.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserInfo {
    @JsonProperty("user_id")
    private Long userId;
    private String username;
    private String role;
    private boolean valid;
}