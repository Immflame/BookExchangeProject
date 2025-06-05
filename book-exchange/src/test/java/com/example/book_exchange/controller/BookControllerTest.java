package com.example.book_exchange.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import com.example.book_exchange.dto.*;
import com.example.book_exchange.exception.*;
import com.example.book_exchange.model.*;
import com.example.book_exchange.model.enums.BookStatus;
import com.example.book_exchange.model.enums.Genre;
import com.example.book_exchange.service.*;
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
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private BookService bookService;
    @Mock private LocationService locationService;
    @Mock private UserService userService;
    @Mock private AuthService authService;

    @InjectMocks private BookController bookController;

    private User testUser;
    private Location testLocation;
    private Book testBook;
    private BookDto testBookDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testUser = new User(1L, "testuser", "password", "USER");
        testLocation = new Location(1L, "Library", "Main library");
        testBook = Book.builder()
                .id(1L)
                .title("The Hobbit")
                .author("J.R.R. Tolkien")
                .description("Fantasy novel")
                .genre(Genre.FANTASY)
                .status(BookStatus.AVAILABLE)
                .location(testLocation)
                .user(testUser)
                .build();

        testBookDto = new BookDto();
        testBookDto.setToken("valid-token");
        testBookDto.setTitle("The Hobbit");
        testBookDto.setAuthor("J.R.R. Tolkien");
        testBookDto.setDescription("Fantasy novel");
        testBookDto.setGenre(Genre.FANTASY);
        testBookDto.setLocationId(1L);
    }

    @Test
    void getAllBooks_SuccessWithoutFilters() throws Exception {
        when(bookService.getAllBooks(null, null)).thenReturn(Collections.singletonList(testBook));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("The Hobbit"));
    }

    @Test
    void getAllBooks_SuccessWithFilters() throws Exception {
        List<Genre> genres = List.of(Genre.FANTASY);
        List<Long> locationIds = List.of(1L);

        when(bookService.getAllBooks(genres, locationIds)).thenReturn(Collections.singletonList(testBook));

        mockMvc.perform(get("/books")
                        .param("genres", "FANTASY")
                        .param("locationIds", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("The Hobbit"));
    }

    @Test
    void getBookById_Success() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(Optional.of(testBook));

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("The Hobbit"));
    }

    @Test
    void getBookById_NotFound() throws Exception {
        when(bookService.getBookById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found with ID: 999"));
    }

    @Test
    void getBookById_InvalidId() throws Exception {
        mockMvc.perform(get("/books/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid book ID"));
    }

    @Test
    void getUserBooks_Success() throws Exception {
        UserInfo userInfo = new UserInfo(1L, "testuser", "USER", true);
        when(authService.validateToken("valid-token")).thenReturn(userInfo);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(bookService.getBooksByUserId(1L)).thenReturn(Collections.singletonList(testBook));

        mockMvc.perform(get("/books/my_books/valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("The Hobbit"));
    }

    @Test
    void getUserBooks_InvalidToken() throws Exception {
        when(authService.validateToken("invalid-token"))
                .thenThrow(new InvalidTokenException("Invalid token"));

        mockMvc.perform(get("/books/my_books/invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void getUserBooks_UserNotFound() throws Exception {
        UserInfo userInfo = new UserInfo(1L, "testuser", "USER", true);
        when(authService.validateToken("valid-token")).thenReturn(userInfo);
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/books/my_books/valid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with ID: 1"));
    }

    @Test
    void createBook_Success() throws Exception {
        UserInfo userInfo = new UserInfo(1L, "testuser", "USER", true);
        when(authService.validateToken("valid-token")).thenReturn(userInfo);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(locationService.getLocationById(1L)).thenReturn(Optional.of(testLocation));
        when(bookService.createBook(any(Book.class))).thenReturn(testBook);

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("The Hobbit"));
    }

    @Test
    void createBook_InvalidData() throws Exception {
        BookDto invalidDto = new BookDto(); // Все поля null

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Token is required")))
                .andExpect(jsonPath("$.message", containsString("Title is required")))
                .andExpect(jsonPath("$.message", containsString("Author is required")));
    }

    @Test
    void createBook_InvalidToken() throws Exception {
        BookDto invalidTokenDto = new BookDto();
        invalidTokenDto.setToken("invalid-token");
        invalidTokenDto.setTitle("The Hobbit");
        invalidTokenDto.setAuthor("J.R.R. Tolkien");

        when(authService.validateToken("invalid-token"))
                .thenThrow(new InvalidTokenException("Invalid token"));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTokenDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void createBook_LocationNotFound() throws Exception {
        UserInfo userInfo = new UserInfo(1L, "testuser", "USER", true);
        when(authService.validateToken("valid-token")).thenReturn(userInfo);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(locationService.getLocationById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location not found with ID: 1"));
    }

    @Test
    void updateBook_Success() throws Exception {
        UserInfo userInfo = new UserInfo(1L, "testuser", "USER", true);
        when(authService.validateToken("valid-token")).thenReturn(userInfo);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(bookService.getBookById(1L)).thenReturn(Optional.of(testBook));
        when(locationService.getLocationById(1L)).thenReturn(Optional.of(testLocation));
        when(bookService.updateBook(anyLong(), any(Book.class))).thenReturn(testBook);

        mockMvc.perform(put("/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Hobbit"));
    }

    @Test
    void updateBook_BookNotFound() throws Exception {
        UserInfo userInfo = new UserInfo(1L, "testuser", "USER", true);
        when(authService.validateToken("valid-token")).thenReturn(userInfo);

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        when(bookService.getBookById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found with ID: 1"));
    }

    @Test
    void updateBook_PermissionDenied() throws Exception {
        User otherUser = new User(2L, "otheruser", "pass", "USER");
        Book otherBook = Book.builder().id(1L).user(otherUser).build();

        UserInfo userInfo = new UserInfo(1L, "testuser", "USER", true);
        when(authService.validateToken("valid-token")).thenReturn(userInfo);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(bookService.getBookById(1L)).thenReturn(Optional.of(otherBook));

        mockMvc.perform(put("/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You don't have permission to modify this book"));
    }

    @Test
    void updateBook_LocationNotFound() throws Exception {
        UserInfo userInfo = new UserInfo(1L, "testuser", "USER", true);
        when(authService.validateToken("valid-token")).thenReturn(userInfo);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(bookService.getBookById(1L)).thenReturn(Optional.of(testBook));
        when(locationService.getLocationById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location not found with ID: 1"));
    }


    @Test
    void deleteBook_BookNotFound() throws Exception {
        UserInfo userInfo = new UserInfo(1L, "testuser", "USER", true);
        when(authService.validateToken("valid-token")).thenReturn(userInfo);
        when(bookService.getBookById(1L)).thenReturn(Optional.empty());

        TokenDto tokenDto = new TokenDto();
        tokenDto.setToken("valid-token");

        mockMvc.perform(delete("/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found"));
    }

    @Test
    void deleteBook_PermissionDenied() throws Exception {
        User otherUser = new User(2L, "otheruser", "pass", "USER");
        Book otherBook = Book.builder().id(1L).user(otherUser).build();

        UserInfo userInfo = new UserInfo(1L, "testuser", "USER", true);
        when(authService.validateToken("valid-token")).thenReturn(userInfo);
        when(bookService.getBookById(1L)).thenReturn(Optional.of(otherBook));

        TokenDto tokenDto = new TokenDto();
        tokenDto.setToken("valid-token");

        mockMvc.perform(delete("/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Permission denied"));
    }

    @Test
    void deleteBook_InvalidToken() throws Exception {
        when(authService.validateToken("invalid-token"))
                .thenThrow(new InvalidTokenException("Invalid token"));

        TokenDto tokenDto = new TokenDto();
        tokenDto.setToken("invalid-token");

        mockMvc.perform(delete("/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }
}