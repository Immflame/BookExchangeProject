package com.example.book_exchange.controller;

import com.example.book_exchange.dto.*;
import com.example.book_exchange.exception.*;
import com.example.book_exchange.model.Book;
import com.example.book_exchange.model.Location;
import com.example.book_exchange.model.User;
import com.example.book_exchange.model.enums.Genre;
import com.example.book_exchange.service.BookService;
import com.example.book_exchange.service.LocationService;
import com.example.book_exchange.service.AuthService;
import com.example.book_exchange.service.UserService;
import com.example.book_exchange.util.ValidationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Book Controller", description = "Operations with books")
@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;

    @Operation(summary = "Get list of all books (filtered by genre and location)")
    @GetMapping
    public ResponseEntity<List<BookResponseDto>> getAllBooks(
            @RequestParam(value = "genres", required = false) List<Genre> genres,
            @RequestParam(value = "locationIds", required = false) List<Long> locationIds) {

        List<Book> books = bookService.getAllBooks(genres, locationIds);
        return ResponseEntity.ok(books.stream()
                .map(this::convertToDto)
                .toList());
    }

    @Operation(summary = "Get book by book_id")
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBookById(@PathVariable("id") Long id) {
        ValidationUtils.validateId(id, "book");
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));
        return ResponseEntity.ok(convertToDto(book));
    }

    @Operation(summary = "Get books by user_id")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookResponseDto>> getBooksByUserId(@PathVariable("userId") Long userId) {
        ValidationUtils.validateId(userId, "user");
        List<Book> books = bookService.getBooksByUserId(userId);
        List<BookResponseDto> bookDtos = books.stream()
                .map(this::convertToDto)
                .toList();
        return ResponseEntity.ok(bookDtos);
    }

    @Operation(summary = "Get list of my books")
    @GetMapping("/my_books/{token}")
    public ResponseEntity<List<BookResponseDto>> getUserBooks(@PathVariable("token") String token){
        UserInfo userInfo = authService.validateToken(token);
        User user = userService.getUserById(userInfo.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userInfo.getUserId()));
        Long user_id = user.getId();
        ValidationUtils.validateId(user_id, "user");
        List<Book> books = bookService.getBooksByUserId(user_id);
        return ResponseEntity.ok(books.stream()
                .map(this::convertToDto)
                .toList());
    }

    @Operation(summary = "Create new book")
    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody BookDto bookDto) {
        UserInfo userInfo = authService.validateToken(bookDto.getToken());
        User user = userService.getUserById(userInfo.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userInfo.getUserId()));

        Book book = convertToEntity(bookDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(book));
    }

    @Operation(summary = "Update book info")
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @PathVariable("id") Long id,
            @Valid @RequestBody BookDto bookDto
    ) {
        ValidationUtils.validateId(id, "book");
        UserInfo userInfo = authService.validateToken(bookDto.getToken());
        User user = userService.getUserById(userInfo.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userInfo.getUserId()));

        Book existingBook = bookService.getBookById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));

        if (!existingBook.getUser().getId().equals(user.getId())) {
            throw new PermissionDeniedException("You don't have permission to modify this book");
        }

        Book updatedBook = bookService.updateBook(id, convertToEntity(bookDto, user));
        return ResponseEntity.ok(updatedBook);
    }

    @Operation(summary = "Delete book by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable("id") Long id,
            @Valid @RequestBody TokenDto tokenDto) {

        UserInfo userInfo = authService.validateToken(tokenDto.getToken());
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (!book.getUser().getId().equals(userInfo.getUserId())) {
            throw new PermissionDeniedException("Permission denied");
        }

        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    private BookResponseDto convertToDto(Book book) {
        return BookResponseDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .description(book.getDescription())
                .genre(book.getGenre())
                .status(book.getStatus())
                .location(book.getLocation() != null ? convertToLocationDto(book.getLocation()) : null)
                .userId(book.getUser() != null ? book.getUser().getId() : null)
                .username(book.getUser() != null ? book.getUser().getUsername() : null)
                .build();
    }

    private Book convertToEntity(BookDto bookDto, User user) {
        Book book = new Book();
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setDescription(bookDto.getDescription());
        book.setGenre(bookDto.getGenre());

        if (bookDto.getLocationId() != null) {
            Location location = locationService.getLocationById(bookDto.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + bookDto.getLocationId()));
            book.setLocation(location);
        }

        book.setUser(user);
        return book;
    }

    private LocationResponseDto convertToLocationDto(Location location) {
        return new LocationResponseDto(location.getId(), location.getName(), location.getDescription());
    }
}