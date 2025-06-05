package com.example.book_exchange.repository;

import com.example.book_exchange.model.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    List<Exchange> findByUser1Id(Long user1Id);
    List<Exchange> findByUser2Id(Long user2Id);
    List<Exchange> findByUser1IdOrUser2Id(Long user1Id, Long user2Id);
    List<Exchange> findByBook1IdOrBook2Id(Long book1Id, Long book2Id);
}
