package com.transaction.modules.transaction.entity;

import com.transaction.modules.transaction.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    private String userId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private Double amount;

    private String currency;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    public Transaction(TransactionEvent transactionEvent) {
        setUserId(transactionEvent.getUserId());
        setType(transactionEvent.getType());
        setAmount(transactionEvent.getAmount());
        setCurrency(transactionEvent.getCurrency());
        setCreatedAt(LocalDateTime.now());
    }
}
