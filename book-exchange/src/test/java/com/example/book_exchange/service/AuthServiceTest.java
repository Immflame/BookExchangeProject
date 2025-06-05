package com.example.book_exchange.service;

import com.example.book_exchange.dto.*;
import com.example.book_exchange.exception.InvalidTokenException;
import com.example.book_exchange.exception.PermissionDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthService authService;

    private final String testToken = "valid.token.123";
    private final String authUrl = "http://auth-service:8081";

    @BeforeEach
    void setup() {
        authService = new AuthService(restTemplate);
        authService.setAuthServiceBaseUrl(authUrl);
    }

    @Test
    void register_Success() {
        AuthRequest request = new AuthRequest("username", "password");
        TokenDto expectedToken = new TokenDto(testToken);

        when(restTemplate.postForEntity(
                eq(authUrl + "/register"),
                eq(request),
                eq(TokenDto.class)
        )).thenReturn(new ResponseEntity<>(expectedToken, HttpStatus.OK));

        TokenDto result = authService.register(request);

        assertEquals(testToken, result.getToken());
        verify(restTemplate).postForEntity(anyString(), any(), any());
    }

    @Test
    void login_Success() {
        AuthRequest request = new AuthRequest("username", "password");
        TokenDto expectedToken = new TokenDto(testToken);

        when(restTemplate.postForEntity(
                eq(authUrl + "/login"),
                eq(request),
                eq(TokenDto.class)
        )).thenReturn(new ResponseEntity<>(expectedToken, HttpStatus.OK));

        TokenDto result = authService.login(request);

        assertEquals(testToken, result.getToken());
    }

    @Test
    void validateToken_ValidToken() {
        UserInfo userInfo = new UserInfo(1L, "user", "USER", true);

        when(restTemplate.postForEntity(
                eq(authUrl + "/validate"),
                any(),
                eq(UserInfo.class)
        )).thenReturn(new ResponseEntity<>(userInfo, HttpStatus.OK));

        UserInfo result = authService.validateToken(testToken);

        assertTrue(result.isValid());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void validateToken_InvalidToken() {
        when(restTemplate.postForEntity(
                eq(authUrl + "/validate"),
                any(),
                eq(UserInfo.class)
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThrows(InvalidTokenException.class,
                () -> authService.validateToken("invalid.token"));
    }

    @Test
    void updateUser_Success() {
        UserUpdateRequestDto request = new UserUpdateRequestDto("oldToken", "newUser", "newPass");
        TokenDto newToken = new TokenDto("new.token.456");

        when(restTemplate.postForEntity(
                eq(authUrl + "/update"),
                eq(request),
                eq(TokenDto.class)
        )).thenReturn(new ResponseEntity<>(newToken, HttpStatus.OK));

        TokenDto result = authService.updateUser(request);

        assertEquals("new.token.456", result.getToken());
    }

    @Test
    void validateAdminToken_ValidAdmin() {
        UserInfo adminInfo = new UserInfo(1L, "admin", "ADMIN", true);

        when(restTemplate.postForEntity(
                eq(authUrl + "/validate"),
                any(),
                eq(UserInfo.class)
        )).thenReturn(new ResponseEntity<>(adminInfo, HttpStatus.OK));

        assertDoesNotThrow(() -> authService.validateAdminToken(testToken));
    }

    @Test
    void validateAdminToken_NotAdmin() {
        UserInfo userInfo = new UserInfo(2L, "user", "USER", true);

        when(restTemplate.postForEntity(
                eq(authUrl + "/validate"),
                any(),
                eq(UserInfo.class)
        )).thenReturn(new ResponseEntity<>(userInfo, HttpStatus.OK));

        assertThrows(PermissionDeniedException.class,
                () -> authService.validateAdminToken(testToken));
    }

    @Test
    void validateAdminToken_InvalidToken() {
        when(restTemplate.postForEntity(
                eq(authUrl + "/validate"),
                any(),
                eq(UserInfo.class)
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThrows(InvalidTokenException.class,
                () -> authService.validateAdminToken("invalid.token"));
    }
}