package com.example.book_exchange.service;


import com.example.book_exchange.exception.InvalidRequestException;
import com.example.book_exchange.exception.PermissionDeniedException;
import com.example.book_exchange.exception.ResourceNotFoundException;
import com.example.book_exchange.model.Book;
import com.example.book_exchange.model.Exchange;
import com.example.book_exchange.model.enums.BookStatus;
import com.example.book_exchange.model.enums.ExchangeStatus;
import com.example.book_exchange.repository.ExchangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Service
public class ExchangeService {

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private BookService bookService;

    @Transactional
    public Exchange updateExchangeStatus(Long id, ExchangeStatus status) {
        Exchange exchange = exchangeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));

        ExchangeStatus currentStatus = exchange.getStatus();

        if (currentStatus == ExchangeStatus.CANCELLED || currentStatus == ExchangeStatus.COMPLETED) {
            throw new InvalidRequestException("Cannot change status from " + currentStatus);
        }

        exchange.setStatus(status);

        if (currentStatus != status) {
            updateBookStatuses(exchange, currentStatus, status);
        }

        return exchangeRepository.save(exchange);
    }

    public List<Exchange> getAllExchanges() {
        return exchangeRepository.findAll();
    }

    public Optional<Exchange> getExchangeById(Long id) {
        return exchangeRepository.findById(id);
    }

    public Exchange createExchange(Exchange exchange) {
        Book book1 = exchange.getBook1();
        Book book2 = exchange.getBook2();

        if (!book1.getUser().getId().equals(exchange.getUser1().getId())) {
            throw new PermissionDeniedException("This book does not belong to you");
        }

        if (book1.getUser().getId().equals(book2.getUser().getId())) {
            throw new InvalidRequestException("Books must belong to different users");
        }

        book1.setStatus(BookStatus.IN_EXCHANGE);
        book2.setStatus(BookStatus.IN_EXCHANGE);

        bookService.updateBook(book1.getId(), book1);
        bookService.updateBook(book2.getId(), book2);

        return exchangeRepository.save(exchange);
    }

    public List<Exchange> getExchangesByUserId(Long userId) {
        return exchangeRepository.findByUser1IdOrUser2Id(userId, userId);
    }

    private void updateBookStatuses(Exchange exchange, ExchangeStatus oldStatus, ExchangeStatus newStatus) {
        Book book1 = exchange.getBook1();
        Book book2 = exchange.getBook2();

        if (newStatus == ExchangeStatus.COMPLETED) {
            book1.setStatus(BookStatus.EXCHANGED);
            book2.setStatus(BookStatus.EXCHANGED);
        } else if (newStatus == ExchangeStatus.CANCELLED && oldStatus == ExchangeStatus.PENDING) {
            book1.setStatus(BookStatus.AVAILABLE);
            book2.setStatus(BookStatus.AVAILABLE);
        } else if (newStatus == ExchangeStatus.PENDING) {
            book1.setStatus(BookStatus.IN_EXCHANGE);
            book2.setStatus(BookStatus.IN_EXCHANGE);
        }

        bookService.updateBook(book1.getId(), book1);
        bookService.updateBook(book2.getId(), book2);
    }

    public void deleteExchange(Long id) {
        if (!exchangeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exchange not found with id: " + id);
        }
        exchangeRepository.deleteById(id);
    }

    public void deleteExchangesByUserId(Long userId) {
        List<Exchange> exchanges = exchangeRepository.findByUser1IdOrUser2Id(userId, userId);
        exchangeRepository.deleteAll(exchanges);
    }

    public void deleteExchangesByBookId(Long bookId) {
        List<Exchange> exchanges = exchangeRepository.findAll().stream()
                .filter(e -> (e.getBook1() != null && e.getBook1().getId().equals(bookId)) ||
                        (e.getBook2() != null && e.getBook2().getId().equals(bookId)))
                .toList();
        exchangeRepository.deleteAll(exchanges);
    }
}