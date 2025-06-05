package com.example.book_exchange.service;

import com.example.book_exchange.exception.ResourceNotFoundException;
import com.example.book_exchange.model.Book;
import com.example.book_exchange.model.enums.BookStatus;
import com.example.book_exchange.model.enums.Genre;
import com.example.book_exchange.repository.BookRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Lazy
    @Autowired
    private ExchangeService exchangeService;

    public List<Book> getAllBooks(List<Genre> genres, List<Long> locationIds) {
        Specification<Book> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Predicate statusPredicate = criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("status"), BookStatus.AVAILABLE),
                    criteriaBuilder.equal(root.get("status"), BookStatus.IN_EXCHANGE)
            );
            predicates.add(statusPredicate);

            if (genres != null && !genres.isEmpty()) {
                predicates.add(root.get("genre").in(genres));
            }

            if (locationIds != null && !locationIds.isEmpty()) {
                predicates.add(root.get("location").get("id").in(locationIds));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return bookRepository.findAll(spec);
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public List<Book> getBooksByUserId(Long user_id) {
        return bookRepository.findByUserId(user_id);
    }

    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    public Book updateBook(Long id, Book bookDetails) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setDescription(bookDetails.getDescription());
        book.setLocation(bookDetails.getLocation());
        book.setGenre(bookDetails.getGenre());
        book.setStatus(bookDetails.getStatus());

        return bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        exchangeService.deleteExchangesByBookId(id);
        bookRepository.deleteById(id);
    }
}