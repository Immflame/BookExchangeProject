package com.example.book_exchange.service;

import com.example.book_exchange.exception.InvalidRequestException;
import com.example.book_exchange.exception.PermissionDeniedException;
import com.example.book_exchange.exception.ResourceNotFoundException;
import com.example.book_exchange.model.Book;
import com.example.book_exchange.model.Exchange;
import com.example.book_exchange.model.User;
import com.example.book_exchange.model.enums.BookStatus;
import com.example.book_exchange.model.enums.ExchangeStatus;
import com.example.book_exchange.repository.ExchangeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {

    @Mock
    private ExchangeRepository exchangeRepository;

    @Mock
    private BookService bookService;

    @InjectMocks
    private ExchangeService exchangeService;

    private Exchange exchange;
    private Book book1;
    private Book book2;
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).username("user1").build();
        user2 = User.builder().id(2L).username("user2").build();
        user3 = User.builder().id(3L).username("user3").build();

        book1 = Book.builder()
                .id(1L)
                .title("Book 1")
                .user(user1)
                .status(BookStatus.AVAILABLE)
                .build();

        book2 = Book.builder()
                .id(2L)
                .title("Book 2")
                .user(user2)
                .status(BookStatus.AVAILABLE)
                .build();

        exchange = Exchange.builder()
                .id(1L)
                .book1(book1)
                .book2(book2)
                .user1(user1)
                .user2(user2)
                .status(ExchangeStatus.PENDING)
                .exchangeDate(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllExchanges_ShouldReturnAllExchanges() {
        when(exchangeRepository.findAll()).thenReturn(Arrays.asList(exchange));
        List<Exchange> result = exchangeService.getAllExchanges();
        assertEquals(1, result.size());
        assertEquals(ExchangeStatus.PENDING, result.get(0).getStatus());
    }

    @Test
    void getExchangeById_ShouldReturnExchange() {
        when(exchangeRepository.findById(1L)).thenReturn(Optional.of(exchange));
        Optional<Exchange> result = exchangeService.getExchangeById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getExchangeById_NotFound_ShouldReturnEmpty() {
        when(exchangeRepository.findById(anyLong())).thenReturn(Optional.empty());
        Optional<Exchange> result = exchangeService.getExchangeById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    void createExchange_ShouldReturnSavedExchange() {
        when(exchangeRepository.save(any(Exchange.class))).thenReturn(exchange);

        Exchange result = exchangeService.createExchange(exchange);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BookStatus.IN_EXCHANGE, book1.getStatus());
        assertEquals(BookStatus.IN_EXCHANGE, book2.getStatus());
    }

    @Test
    void createExchange_SameUserBooks_ShouldThrowException() {
        book2.setUser(user1);
        Exchange invalidExchange = Exchange.builder()
                .book1(book1)
                .book2(book2)
                .user1(user1)
                .build();

        lenient().when(bookService.getBookById(1L)).thenReturn(Optional.of(book1));
        lenient().when(bookService.getBookById(2L)).thenReturn(Optional.of(book2));

        assertThrows(InvalidRequestException.class,
                () -> exchangeService.createExchange(invalidExchange));
    }

    @Test
    void createExchange_BookNotBelongToUser_ShouldThrowException() {
        book1.setUser(user3);
        Exchange invalidExchange = Exchange.builder()
                .book1(book1)
                .book2(book2)
                .user1(user1)
                .build();

        lenient().when(bookService.getBookById(1L)).thenReturn(Optional.of(book1));
        lenient().when(bookService.getBookById(2L)).thenReturn(Optional.of(book2));

        assertThrows(PermissionDeniedException.class,
                () -> exchangeService.createExchange(invalidExchange));
    }

    @Test
    void getExchangesByUserId_ShouldReturnUserExchanges() {
        when(exchangeRepository.findByUser1IdOrUser2Id(1L, 1L)).thenReturn(Arrays.asList(exchange));
        List<Exchange> result = exchangeService.getExchangesByUserId(1L);
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(e ->
                e.getUser1().getId().equals(1L) ||
                        e.getUser2().getId().equals(1L)
        ));
    }

    @Test
    void getExchangesByUserId_NoExchanges_ShouldReturnEmptyList() {
        when(exchangeRepository.findByUser1IdOrUser2Id(anyLong(), anyLong())).thenReturn(Collections.emptyList());
        List<Exchange> result = exchangeService.getExchangesByUserId(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateExchangeStatus_PendingToCompleted_ShouldUpdateBookStatuses() {
        when(exchangeRepository.findById(1L)).thenReturn(Optional.of(exchange));
        when(exchangeRepository.save(any(Exchange.class))).thenReturn(exchange);

        Exchange result = exchangeService.updateExchangeStatus(1L, ExchangeStatus.COMPLETED);
        assertEquals(ExchangeStatus.COMPLETED, result.getStatus());
        assertEquals(BookStatus.EXCHANGED, book1.getStatus());
        assertEquals(BookStatus.EXCHANGED, book2.getStatus());
    }

    @Test
    void updateExchangeStatus_PendingToCancelled_ShouldRevertBookStatuses() {
        when(exchangeRepository.findById(1L)).thenReturn(Optional.of(exchange));
        when(exchangeRepository.save(any(Exchange.class))).thenReturn(exchange);

        Exchange result = exchangeService.updateExchangeStatus(1L, ExchangeStatus.CANCELLED);
        assertEquals(ExchangeStatus.CANCELLED, result.getStatus());
        assertEquals(BookStatus.AVAILABLE, book1.getStatus());
        assertEquals(BookStatus.AVAILABLE, book2.getStatus());
    }

    @Test
    void updateExchangeStatus_CompletedToCancelled_ShouldThrowException() {
        exchange.setStatus(ExchangeStatus.COMPLETED);
        when(exchangeRepository.findById(1L)).thenReturn(Optional.of(exchange));

        assertThrows(InvalidRequestException.class,
                () -> exchangeService.updateExchangeStatus(1L, ExchangeStatus.CANCELLED));

        verify(bookService, never()).updateBook(anyLong(), any(Book.class));
    }

    @Test
    void updateExchangeStatus_SameStatus_ShouldNotChangeBookStatuses() {
        when(exchangeRepository.findById(1L)).thenReturn(Optional.of(exchange));
        when(exchangeRepository.save(any(Exchange.class))).thenReturn(exchange);

        Exchange result = exchangeService.updateExchangeStatus(1L, ExchangeStatus.PENDING);
        assertEquals(ExchangeStatus.PENDING, result.getStatus());
        assertEquals(BookStatus.AVAILABLE, book1.getStatus());
        verify(bookService, never()).updateBook(anyLong(), any(Book.class));
    }

    @Test
    void updateExchangeStatus_InvalidTransition_ShouldThrowException() {
        exchange.setStatus(ExchangeStatus.CANCELLED);
        when(exchangeRepository.findById(1L)).thenReturn(Optional.of(exchange));

        assertThrows(InvalidRequestException.class,
                () -> exchangeService.updateExchangeStatus(1L, ExchangeStatus.COMPLETED));
    }

    @Test
    void updateExchangeStatus_ExchangeNotFound_ShouldThrowException() {
        when(exchangeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> exchangeService.updateExchangeStatus(1L, ExchangeStatus.COMPLETED));
    }

    @Test
    void updateExchangeStatus_PendingToPending_ShouldNotChangeBookStatuses() {
        when(exchangeRepository.findById(1L)).thenReturn(Optional.of(exchange));
        when(exchangeRepository.save(any(Exchange.class))).thenReturn(exchange);

        Exchange result = exchangeService.updateExchangeStatus(1L, ExchangeStatus.PENDING);
        assertEquals(ExchangeStatus.PENDING, result.getStatus());
        verify(bookService, never()).updateBook(anyLong(), any(Book.class));
    }

    @Test
    void updateExchangeStatus_CancelledToPending_ShouldThrowException() {
        exchange.setStatus(ExchangeStatus.CANCELLED);
        when(exchangeRepository.findById(1L)).thenReturn(Optional.of(exchange));

        assertThrows(InvalidRequestException.class,
                () -> exchangeService.updateExchangeStatus(1L, ExchangeStatus.PENDING));
    }

    @Test
    void updateExchangeStatus_CompletedToPending_ShouldThrowException() {
        exchange.setStatus(ExchangeStatus.COMPLETED);
        when(exchangeRepository.findById(1L)).thenReturn(Optional.of(exchange));

        assertThrows(InvalidRequestException.class,
                () -> exchangeService.updateExchangeStatus(1L, ExchangeStatus.PENDING));
    }
}