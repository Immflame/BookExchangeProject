package com.example.book_exchange.service;

import com.example.book_exchange.dto.*;
import com.example.book_exchange.exception.InvalidTokenException;
import com.example.book_exchange.exception.PermissionDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Setter
public class AuthService {
    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceBaseUrl;

    public TokenDto register(AuthRequest request) {
        String url = authServiceBaseUrl + "/register";
        ResponseEntity<TokenDto> response = restTemplate.postForEntity(url, request, TokenDto.class);
        validateAuthResponse(response);
        return response.getBody();
    }

    public TokenDto login(AuthRequest request) {
        String url = authServiceBaseUrl + "/login";
        ResponseEntity<TokenDto> response = restTemplate.postForEntity(url, request, TokenDto.class);
        validateAuthResponse(response);
        return response.getBody();
    }

    public UserInfo validateToken(String token) {
        String url = authServiceBaseUrl + "/validate";
        ResponseEntity<UserInfo> response = restTemplate.postForEntity(
                url,
                new TokenRequest(token),
                UserInfo.class
        );
        if (response.getBody() == null || !response.getBody().isValid()) {
            throw new InvalidTokenException("Invalid token");
        }
        return response.getBody();
    }

    public TokenDto updateUser(UserUpdateRequestDto request) {
        String url = authServiceBaseUrl + "/update";
        ResponseEntity<TokenDto> response = restTemplate.postForEntity(url, request, TokenDto.class);
        validateAuthResponse(response);
        return response.getBody();
    }

    public void validateAdminToken(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException("Authorization token is required");
        }
        UserInfo userInfo = validateToken(token);
        if (!"admin".equalsIgnoreCase(userInfo.getRole())) {
            throw new PermissionDeniedException("Admin role required");
        }
    }

    private void validateAuthResponse(ResponseEntity<?> response) {
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Auth service request failed");
        }
    }

    private record TokenRequest(String token) {}
}
