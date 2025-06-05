package com.example.book_exchange.controller;

import com.example.book_exchange.dto.*;
import com.example.book_exchange.exception.*;
import com.example.book_exchange.model.User;
import com.example.book_exchange.service.AuthService;
import com.example.book_exchange.service.UserService;
import com.example.book_exchange.util.ValidationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Controller", description = "Operations with users, registration and auth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @Operation(summary = "Get all users")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream()
                .map(this::convertToDto)
                .toList());
    }

    @Operation(summary = "Get user by user_id")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        ValidationUtils.validateId(id, "user");
        return ResponseEntity.ok(convertToDto(
                userService.getUserById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"))
        ));
    }

    @Operation(summary = "Get user by username")
    @GetMapping("/get_user_by_username/{username}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable String username) {
        return ResponseEntity.ok(convertToDto(
                userService.getUserByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"))
        ));
    }

    @Operation(summary = "Get info about me")
    @PostMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@Valid @RequestBody TokenDto tokenDto) {
        return ResponseEntity.ok(convertToDto(validateUser(tokenDto.getToken())));
    }

    @Operation(summary = "Register")
    @PostMapping("/register")
    public ResponseEntity<TokenDto> register(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Update username or password")
    @PutMapping("/")
    public ResponseEntity<TokenDto> updateUser(@Valid @RequestBody UserUpdateRequestDto request) {
        return ResponseEntity.ok(authService.updateUser(request));
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/")
    public ResponseEntity<Void> deleteUser(@Valid @RequestBody TokenDto tokenDto) {

        User user = validateUser(tokenDto.getToken());
        userService.deleteUser(user.getId());
        return ResponseEntity.noContent().build();
    }

    private User validateUser(String token) {
        UserInfo userInfo = authService.validateToken(token);
        return userService.getUserById(userInfo.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponseDto convertToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
