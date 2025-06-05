package com.example.book_exchange.controller;

import com.example.book_exchange.dto.*;
import com.example.book_exchange.exception.*;
import com.example.book_exchange.model.Book;
import com.example.book_exchange.model.Exchange;
import com.example.book_exchange.model.User;
import com.example.book_exchange.model.enums.ExchangeStatus;
import com.example.book_exchange.service.BookService;
import com.example.book_exchange.service.ExchangeService;
import com.example.book_exchange.service.AuthService;
import com.example.book_exchange.service.UserService;
import com.example.book_exchange.util.ValidationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Exchange Controller", description = "Operations with exchanges")
@RestController
@RequestMapping("/exchanges")
public class ExchangeController {

    @Autowired private ExchangeService exchangeService;
    @Autowired private BookService bookService;
    @Autowired private UserService userService;
    @Autowired private AuthService authService;

    @Operation(summary = "Get all exchanges (only for admin)")
    @GetMapping("/get_all_exchanges/{token}")
    public ResponseEntity<List<ExchangeResponseDto>> getAllExchanges(@PathVariable("token") String token) {

        authService.validateAdminToken(token);
        return ResponseEntity.ok(exchangeService.getAllExchanges().stream()
                .map(this::convertToDto)
                .toList());
    }

    @Operation(summary = "Get list of my exchanges")
    @GetMapping("/my_exchanges/{token}")
    public ResponseEntity<List<ExchangeResponseDto>> getMyExchanges(@PathVariable("token") String token) {
        UserInfo userInfo = authService.validateToken(token);
        List<Exchange> exchanges = exchangeService.getExchangesByUserId(userInfo.getUserId());
        return ResponseEntity.ok(
                exchanges.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Get exchange info by exchange_id")
    @GetMapping("/{id}")
    public ResponseEntity<ExchangeResponseDto> getExchangeById(@PathVariable("id") Long id) {
        ValidationUtils.validateId(id, "exchange");
        Exchange exchange = exchangeService.getExchangeById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found with ID: " + id));
        return ResponseEntity.ok(convertToDto(exchange));
    }

    @Operation(summary = "Create new exchange")
    @PostMapping
    public ResponseEntity<Exchange> createExchange(@Valid @RequestBody ExchangeRequestDto dto) {
        UserInfo userInfo = authService.validateToken(dto.getToken());
        User currentUser = userService.getUserById(userInfo.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userInfo.getUserId()));
        return ResponseEntity.status(201)
                .body(exchangeService.createExchange(convertToEntity(dto, currentUser)));
    }

    @Operation(summary = "Update exchange status")
    @PatchMapping("/{id}")
    public ResponseEntity<ExchangeResponseDto> updateExchangeStatus(
            @PathVariable Long id,
            @Valid @RequestBody TokenDto tokenDto,
            @RequestParam(value = "status", required = false) ExchangeStatus exchangeStatus
    ) {

        UserInfo userInfo = authService.validateToken(tokenDto.getToken());
        Exchange exchange = exchangeService.getExchangeById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));

        if (!exchange.getUser1().getId().equals(userInfo.getUserId())
                && !exchange.getUser2().getId().equals(userInfo.getUserId())) {
            throw new PermissionDeniedException("You are not a participant of this exchange");
        }

        Exchange updatedExchange = exchangeService.updateExchangeStatus(id, exchangeStatus);
        return ResponseEntity.ok(convertToDto(updatedExchange));
    }

    private ExchangeResponseDto convertToDto(Exchange exchange) {
        return ExchangeResponseDto.builder()
                .id(exchange.getId())
                .book1(convertToBookDto(exchange.getBook1()))
                .book2(convertToBookDto(exchange.getBook2()))
                .user1(convertToUserDto(exchange.getUser1()))
                .user2(convertToUserDto(exchange.getUser2()))
                .status(exchange.getStatus())
                .exchangeDate(exchange.getExchangeDate())
                .build();
    }

    private Exchange convertToEntity(ExchangeRequestDto dto, User user) {
        Book book1 = bookService.getBookById(dto.getBook1Id())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + dto.getBook1Id()));
        Book book2 = bookService.getBookById(dto.getBook2Id())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + dto.getBook2Id()));

        if (!book1.getUser().getId().equals(user.getId())){
            throw new PermissionDeniedException("This book does not belong to you");
        }

        if (book1.getUser().getId().equals(book2.getUser().getId())) {
            throw new InvalidRequestException("Books must belong to different users");
        }


        return Exchange.builder()
                .book1(book1)
                .book2(book2)
                .user1(user)
                .user2(book2.getUser())
                .status(ExchangeStatus.PENDING)
                .exchangeDate(LocalDateTime.now())
                .build();
    }

    private BookResponseDto convertToBookDto(Book book) {
        return BookResponseDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .description(book.getDescription())
                .genre(book.getGenre())
                .status(book.getStatus())
                .location(book.getLocation() != null ?
                        new LocationResponseDto(
                                book.getLocation().getId(),
                                book.getLocation().getName(),
                                book.getLocation().getDescription())
                        : null)
                .username(book.getUser() != null ? book.getUser().getUsername() : null)
                .userId(book.getUser() != null ? book.getUser().getId() : null)
                .build();
    }

    private UserResponseDto convertToUserDto(User user) {
        return new UserResponseDto(user.getId(), user.getUsername(), user.getRole());
    }
}
