package com.example.book_exchange.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.book_exchange.dto.AuthRequest;
import com.example.book_exchange.dto.TokenDto;
import com.example.book_exchange.dto.UserInfo;
import com.example.book_exchange.dto.UserUpdateRequestDto;
import com.example.book_exchange.exception.GlobalExceptionHandler;
import com.example.book_exchange.exception.InvalidTokenException;
import com.example.book_exchange.model.User;
import com.example.book_exchange.service.AuthService;
import com.example.book_exchange.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllUsers_Success() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.singletonList(
                new User(1L, "user1", "pass1", "USER")
        ));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("user1"));
    }

    @Test
    void getUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(
                Optional.of(new User(1L, "user1", "pass1", "USER"))
        );

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    void getUserById_NotFound() throws Exception {
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getUserById_InvalidId() throws Exception {
        mockMvc.perform(get("/users/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid user ID"));
    }

    @Test
    void getCurrentUser_Success() throws Exception {
        User user = new User(1L, "user1", "pass1", "USER");
        when(authService.validateToken("valid-token")).thenReturn(
                new UserInfo(1L, "user1", "USER", true)
        );
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenDto("valid-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    void getCurrentUser_InvalidToken() throws Exception {
        when(authService.validateToken("invalid-token"))
                .thenThrow(new InvalidTokenException("Invalid token"));

        mockMvc.perform(post("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenDto("invalid-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void getCurrentUser_UserNotFound() throws Exception {
        when(authService.validateToken("valid-token")).thenReturn(
                new UserInfo(1L, "user1", "USER", true)
        );
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenDto("valid-token"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void register_Success() throws Exception {
        AuthRequest request = new AuthRequest("newuser", "password");
        when(authService.register(request)).thenReturn(new TokenDto("token123"));

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"));
    }

    @Test
    void register_InvalidData() throws Exception {
        AuthRequest request = new AuthRequest("", ""); // Пустые поля

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Username is required")))
                .andExpect(jsonPath("$.message", containsString("Password is required")));
    }

    @Test
    void login_Success() throws Exception {
        AuthRequest request = new AuthRequest("user", "pass");
        when(authService.login(request)).thenReturn(new TokenDto("token123"));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"));
    }

    @Test
    void updateUser_Success() throws Exception {
        UserUpdateRequestDto request = new UserUpdateRequestDto("valid-token", "newuser", "newpass");
        when(authService.updateUser(request)).thenReturn(new TokenDto("new-token"));

        mockMvc.perform(put("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-token"));
    }

    @Test
    void updateUser_InvalidToken() throws Exception {
        UserUpdateRequestDto request = new UserUpdateRequestDto("invalid-token", "newuser", "newpass");
        when(authService.updateUser(request))
                .thenThrow(new InvalidTokenException("Invalid token"));

        mockMvc.perform(put("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void updateUser_InvalidData() throws Exception {
        UserUpdateRequestDto request = new UserUpdateRequestDto("token", "", "");

        mockMvc.perform(put("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("must not be blank")));
    }

}