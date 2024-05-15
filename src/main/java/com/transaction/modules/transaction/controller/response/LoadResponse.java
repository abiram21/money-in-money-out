package com.transaction.modules.transaction.controller.response;

import com.transaction.modules.transaction.controller.request.Amount;
import com.transaction.modules.transaction.entity.TransactionEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadResponse {

    private String userId;

    private String messageId;

    private Amount balance;

    public LoadResponse(TransactionEvent transactionEvent, Amount amount) {
        setUserId(transactionEvent.getUserId());
        setMessageId(transactionEvent.getMessageId());
        setBalance(amount);
    }
}
