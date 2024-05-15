package com.transaction.modules.transaction.controller.request;

import com.transaction.modules.transaction.entity.TransactionEvent;
import com.transaction.modules.transaction.enums.TransactionType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Amount {

    @NotEmpty(message = "Amount cannot be empty.")
    private String amount;

    @NotEmpty(message = "Currency cannot be empty.")
    private String currency;

    @NotNull(message = "Debit or credit type is required.")
    private TransactionType debitOrCredit;

    public Amount(TransactionEvent transactionEvent) {
        setAmount(transactionEvent.getRunningBalance().toString());
        setCurrency(transactionEvent.getCurrency());
        setDebitOrCredit(transactionEvent.getType());
    }
}
