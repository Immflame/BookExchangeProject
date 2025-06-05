package com.example.book_exchange.service;

import com.example.book_exchange.exception.ResourceNotFoundException;
import com.example.book_exchange.model.Book;
import com.example.book_exchange.model.Location;
import com.example.book_exchange.model.User;
import com.example.book_exchange.model.enums.BookStatus;
import com.example.book_exchange.model.enums.Genre;
import com.example.book_exchange.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private User user;
    private Location location;
    private Book updatedBook;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testUser")
                .build();

        location = Location.builder()
                .id(1L)
                .name("Test Location")
                .build();

        book = Book.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .description("Test Description")
                .location(location)
                .user(user)
                .genre(Genre.FANTASY)
                .status(BookStatus.AVAILABLE)
                .build();

        updatedBook = Book.builder()
                .id(1L)
                .title("Updated Title")
                .author("Updated Author")
                .description("Updated Description")
                .location(Location.builder().id(2L).build())
                .user(User.builder().id(2L).build())
                .genre(Genre.SCIENCE_FICTION)
                .status(BookStatus.IN_EXCHANGE)
                .build();
    }

    @Test
    void getAllBooks_WithFilters_ShouldReturnFilteredBooks() {
        List<Genre> genres = Arrays.asList(Genre.FANTASY);
        List<Long> locationIds = Arrays.asList(1L);
        when(bookRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(book));

        List<Book> result = bookService.getAllBooks(genres, locationIds);

        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
        assertEquals(BookStatus.AVAILABLE, result.get(0).getStatus());
    }

    @Test
    void getAllBooks_NoFilters_ShouldReturnAllAvailableBooks() {
        when(bookRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(book));

        List<Book> result = bookService.getAllBooks(null, null);

        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(b ->
                b.getStatus() == BookStatus.AVAILABLE ||
                        b.getStatus() == BookStatus.IN_EXCHANGE
        ));
    }

    @Test
    void getBookById_ShouldReturnBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.getBookById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Book", result.get().getTitle());
    }

    @Test
    void getBookById_NotFound_ShouldReturnEmpty() {
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Book> result = bookService.getBookById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void getBooksByUserId_ShouldReturnUserBooks() {
        Long userId = 1L;
        when(bookRepository.findByUserId(userId)).thenReturn(Arrays.asList(book));

        List<Book> result = bookService.getBooksByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUser().getId());
    }

    @Test
    void getBooksByUserId_NoBooks_ShouldReturnEmptyList() {
        Long userId = 2L;
        when(bookRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<Book> result = bookService.getBooksByUserId(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void createBook_ShouldReturnSavedBook() {
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.createBook(book);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BookStatus.AVAILABLE, result.getStatus());
    }

    @Test
    void updateBook_ShouldUpdateAllFields() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.updateBook(1L, updatedBook);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Author", result.getAuthor());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(2L, result.getLocation().getId());
        assertEquals(Genre.SCIENCE_FICTION, result.getGenre());
        assertEquals(BookStatus.IN_EXCHANGE, result.getStatus());
    }

    @Test
    void updateBook_ShouldThrowWhenNotFound() {
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookService.updateBook(1L, updatedBook));
    }


    @Test
    void deleteBook_ShouldThrowWhenNotFound() {
        when(bookRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> bookService.deleteBook(1L));
        verify(bookRepository, never()).deleteById(anyLong());
    }
}