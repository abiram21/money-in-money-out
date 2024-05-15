package com.transaction.modules.transaction.entity;

import com.transaction.modules.transaction.controller.request.AuthorizationRequest;
import com.transaction.modules.transaction.controller.request.LoadRequest;
import com.transaction.modules.transaction.enums.TransactionEventStatus;
import com.transaction.modules.transaction.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.common.aliasing.qual.Unique;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table
@NoArgsConstructor
public class TransactionEvent {
    @Id
    @Unique
    private String messageId;

    private String userId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private Double amount;

    private Double runningBalance;

    private String currency;

    @Enumerated(EnumType.STRING)
    private TransactionEventStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    public TransactionEvent(LoadRequest loadRequest, Double balance) {
        setMessageId(loadRequest.getMessageId());
        setUserId(loadRequest.getUserId());
        setType(loadRequest.getTransactionAmount().getDebitOrCredit());
        setAmount(Double.valueOf(loadRequest.getTransactionAmount().getAmount()));
        setRunningBalance(balance);
        setCurrency(loadRequest.getTransactionAmount().getCurrency());
        setStatus(TransactionEventStatus.PENDING);
        setCreatedAt(LocalDateTime.now());
    }

    public TransactionEvent(AuthorizationRequest authorizationRequest, Double balance, TransactionEventStatus transactionEventStatus) {
        setMessageId(authorizationRequest.getMessageId());
        setUserId(authorizationRequest.getUserId());
        setType(authorizationRequest.getTransactionAmount().getDebitOrCredit());
        setAmount(Double.valueOf(authorizationRequest.getTransactionAmount().getAmount()));
        setRunningBalance(balance);
        setCurrency(authorizationRequest.getTransactionAmount().getCurrency());
        setStatus(transactionEventStatus);
        setCreatedAt(LocalDateTime.now());
    }
}

