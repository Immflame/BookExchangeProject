package com.example.book_exchange.model;

import com.example.book_exchange.model.enums.ExchangeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "exchanges")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Exchange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book1_id")
    private Book book1;

    @ManyToOne
    @JoinColumn(name = "book2_id")
    private Book book2;

    @ManyToOne
    @JoinColumn(name = "user1_id")
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id")
    private User user2;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ExchangeStatus status = ExchangeStatus.PENDING;

    private LocalDateTime exchangeDate;
}

