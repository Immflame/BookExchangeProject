package com.example.book_exchange.service;

import com.example.book_exchange.exception.ResourceNotFoundException;
import com.example.book_exchange.model.Book;
import com.example.book_exchange.model.Exchange;
import com.example.book_exchange.model.Review;
import com.example.book_exchange.model.User;
import com.example.book_exchange.repository.BookRepository;
import com.example.book_exchange.repository.ExchangeRepository;
import com.example.book_exchange.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookService bookService;

    @Autowired
    private ExchangeService exchangeService;

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {return userRepository.findByUsername(username);}

    @Transactional
    public void deleteUser(Long id) {
        reviewService.deleteReviewsByUserId(id);

        List<Book> userBooks = bookService.getBooksByUserId(id);
        for (Book book : userBooks) {
            bookService.deleteBook(book.getId());
        }

        exchangeService.deleteExchangesByUserId(id);

        userRepository.deleteById(id);
    }
}
