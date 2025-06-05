package com.example.book_exchange.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import com.example.book_exchange.dto.*;
import com.example.book_exchange.exception.*;
import com.example.book_exchange.model.*;
import com.example.book_exchange.model.enums.BookStatus;
import com.example.book_exchange.model.enums.ExchangeStatus;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ExchangeControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private ExchangeService exchangeService;
    @Mock private BookService bookService;
    @Mock private UserService userService;
    @Mock private AuthService authService;

    @InjectMocks private ExchangeController exchangeController;

    private User user1;
    private User user2;
    private Book book1;
    private Book book2;
    private Exchange testExchange;
    private ExchangeRequestDto testRequestDto;
    private TokenDto tokenDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(exchangeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        user1 = new User(1L, "user1", "pass", "USER");
        user2 = new User(2L, "user2", "pass", "USER");

        Location location = new Location(1L, "Library", "Main library");

        book1 = Book.builder()
                .id(1L)
                .title("Book 1")
                .author("Author 1")
                .user(user1)
                .status(BookStatus.AVAILABLE)
                .location(location)
                .build();

        book2 = Book.builder()
                .id(2L)
                .title("Book 2")
                .author("Author 2")
                .user(user2)
                .status(BookStatus.AVAILABLE)
                .location(location)
                .build();

        testExchange = Exchange.builder()
                .id(1L)
                .book1(book1)
                .book2(book2)
                .user1(user1)
                .user2(user2)
                .status(ExchangeStatus.PENDING)
                .exchangeDate(LocalDateTime.now())
                .build();

        testRequestDto = new ExchangeRequestDto();
        testRequestDto.setToken("valid-token");
        testRequestDto.setBook1Id(1L);
        testRequestDto.setBook2Id(2L);

        tokenDto = new TokenDto();
        tokenDto.setToken("valid-token");
    }

    // Тесты для getAllExchanges
    @Test
    void getAllExchanges_Success() throws Exception {
        when(exchangeService.getAllExchanges()).thenReturn(Collections.singletonList(testExchange));
        doNothing().when(authService).validateAdminToken("admin-token");

        mockMvc.perform(get("/exchanges/get_all_exchanges/admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllExchanges_InvalidToken() throws Exception {
        doThrow(new InvalidTokenException("Invalid token"))
                .when(authService).validateAdminToken("invalid-token");

        mockMvc.perform(get("/exchanges/get_all_exchanges/invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void getAllExchanges_NotAdmin() throws Exception {
        doThrow(new PermissionDeniedException("Admin role required"))
                .when(authService).validateAdminToken("user-token");

        mockMvc.perform(get("/exchanges/get_all_exchanges/user-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Admin role required"));
    }

    // Тесты для getMyExchanges
    @Test
    void getMyExchanges_Success() throws Exception {
        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "user1", "USER", true));
        when(exchangeService.getExchangesByUserId(1L)).thenReturn(Collections.singletonList(testExchange));

        mockMvc.perform(get("/exchanges/my_exchanges/valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getMyExchanges_InvalidToken() throws Exception {
        when(authService.validateToken("invalid-token"))
                .thenThrow(new InvalidTokenException("Invalid token"));

        mockMvc.perform(get("/exchanges/my_exchanges/invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    // Тесты для getExchangeById
    @Test
    void getExchangeById_Success() throws Exception {
        when(exchangeService.getExchangeById(1L)).thenReturn(Optional.of(testExchange));

        mockMvc.perform(get("/exchanges/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getExchangeById_NotFound() throws Exception {
        when(exchangeService.getExchangeById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/exchanges/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Exchange not found with ID: 999"));
    }

    @Test
    void getExchangeById_InvalidId() throws Exception {
        mockMvc.perform(get("/exchanges/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid exchange ID"));
    }

    // Тесты для createExchange
    @Test
    void createExchange_Success() throws Exception {
        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "user1", "USER", true));
        when(userService.getUserById(1L)).thenReturn(Optional.of(user1));
        when(bookService.getBookById(1L)).thenReturn(Optional.of(book1));
        when(bookService.getBookById(2L)).thenReturn(Optional.of(book2));
        when(exchangeService.createExchange(any(Exchange.class))).thenReturn(testExchange);

        mockMvc.perform(post("/exchanges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createExchange_InvalidToken() throws Exception {
        when(authService.validateToken("invalid-token"))
                .thenThrow(new InvalidTokenException("Invalid token"));

        ExchangeRequestDto invalidDto = new ExchangeRequestDto();
        invalidDto.setToken("invalid-token");
        invalidDto.setBook1Id(1L);
        invalidDto.setBook2Id(2L);

        mockMvc.perform(post("/exchanges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void createExchange_BookNotFound() throws Exception {
        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "user1", "USER", true));
        when(userService.getUserById(1L)).thenReturn(Optional.of(user1));
        when(bookService.getBookById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/exchanges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found: 1"));
    }

    @Test
    void createExchange_SameUserBooks() throws Exception {
        Book sameUserBook = Book.builder().id(3L).user(user1).build();

        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "user1", "USER", true));
        when(userService.getUserById(1L)).thenReturn(Optional.of(user1));
        when(bookService.getBookById(1L)).thenReturn(Optional.of(book1));
        when(bookService.getBookById(3L)).thenReturn(Optional.of(sameUserBook));

        ExchangeRequestDto sameUserDto = new ExchangeRequestDto();
        sameUserDto.setToken("valid-token");
        sameUserDto.setBook1Id(1L);
        sameUserDto.setBook2Id(3L);

        mockMvc.perform(post("/exchanges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sameUserDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Books must belong to different users"));
    }

    @Test
    void createExchange_PermissionDenied() throws Exception {
        Book otherUserBook = Book.builder()
                .id(1L)
                .user(user2)
                .build();

        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "user1", "USER", true));
        when(userService.getUserById(1L)).thenReturn(Optional.of(user1));

        when(bookService.getBookById(1L)).thenReturn(Optional.of(otherUserBook));
        when(bookService.getBookById(2L)).thenReturn(Optional.of(book2));

        mockMvc.perform(post("/exchanges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This book does not belong to you"));
    }

    @Test
    void createExchange_InvalidBookIds() throws Exception {
        ExchangeRequestDto invalidDto = new ExchangeRequestDto();
        invalidDto.setToken("valid-token");
        invalidDto.setBook1Id(null);
        invalidDto.setBook2Id(2L);

        mockMvc.perform(post("/exchanges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Book1 ID is required")));
    }

    // Тесты для updateExchangeStatus
    @Test
    void updateExchangeStatus_Success() throws Exception {
        Exchange completedExchange = new Exchange();
        completedExchange.setId(1L);
        completedExchange.setBook1(book1);
        completedExchange.setBook2(book2);
        completedExchange.setUser1(user1);
        completedExchange.setUser2(user2);
        completedExchange.setStatus(ExchangeStatus.COMPLETED);
        completedExchange.setExchangeDate(testExchange.getExchangeDate());

        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "user1", "USER", true));
        when(exchangeService.getExchangeById(1L)).thenReturn(Optional.of(testExchange));
        when(exchangeService.updateExchangeStatus(1L, ExchangeStatus.COMPLETED))
                .thenReturn(completedExchange);

        mockMvc.perform(patch("/exchanges/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto))
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void updateExchangeStatus_NotFound() throws Exception {
        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "user1", "USER", true));
        when(exchangeService.getExchangeById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/exchanges/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto))
                        .param("status", "COMPLETED"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Exchange not found"));
    }

    @Test
    void updateExchangeStatus_PermissionDenied() throws Exception {
        User otherUser = new User(3L, "user3", "pass", "USER");
        Exchange otherExchange = new Exchange();
        otherExchange.setId(1L);
        otherExchange.setBook1(book1);
        otherExchange.setBook2(book2);
        otherExchange.setUser1(otherUser);
        otherExchange.setUser2(otherUser);
        otherExchange.setStatus(ExchangeStatus.PENDING);
        otherExchange.setExchangeDate(testExchange.getExchangeDate());

        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "user1", "USER", true));
        when(exchangeService.getExchangeById(1L)).thenReturn(Optional.of(otherExchange));

        mockMvc.perform(patch("/exchanges/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto))
                        .param("status", "COMPLETED"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not a participant of this exchange"));
    }

    @Test
    void updateExchangeStatus_InvalidToken() throws Exception {
        when(authService.validateToken("invalid-token"))
                .thenThrow(new InvalidTokenException("Invalid token"));

        TokenDto invalidToken = new TokenDto();
        invalidToken.setToken("invalid-token");

        mockMvc.perform(patch("/exchanges/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidToken))
                        .param("status", "COMPLETED"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void updateExchangeStatus_InvalidStatus() throws Exception {
        mockMvc.perform(patch("/exchanges/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto))
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Valid values are: [PENDING, COMPLETED, CANCELLED]")));
    }

    @Test
    void updateExchangeStatus_NoStatusParam() throws Exception {
        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "user1", "USER", true));
        when(exchangeService.getExchangeById(1L)).thenReturn(Optional.of(testExchange));
        when(exchangeService.updateExchangeStatus(1L, null))
                .thenReturn(testExchange);

        mockMvc.perform(patch("/exchanges/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto)))
                .andExpect(status().isOk());
    }

    // Граничные случаи
    @Test
    void createExchange_SameBookIds() throws Exception {
        ExchangeRequestDto sameBookDto = new ExchangeRequestDto();
        sameBookDto.setToken("valid-token");
        sameBookDto.setBook1Id(1L);
        sameBookDto.setBook2Id(1L);

        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "user1", "USER", true));
        when(userService.getUserById(1L)).thenReturn(Optional.of(user1));
        when(bookService.getBookById(1L)).thenReturn(Optional.of(book1));

        mockMvc.perform(post("/exchanges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sameBookDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Books must belong to different users"));
    }
}
