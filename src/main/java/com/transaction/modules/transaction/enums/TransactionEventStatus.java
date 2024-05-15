package com.transaction.modules.transaction.enums;

public enum TransactionEventStatus {
    PENDING, // If the event is created successfully
    PROCESSED, // If the transaction is created successfully
    FAILED // If the authorization request has insufficient balance
}
