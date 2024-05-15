package com.transaction.modules.transaction.controller.response;

import com.transaction.modules.transaction.controller.request.Amount;
import com.transaction.modules.transaction.enums.AuthorizationStatus;
import com.transaction.modules.transaction.entity.TransactionEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorizationResponse {
    private String userId;

    private String messageId;

    private AuthorizationStatus responseCode;

    private Amount balance;

    public AuthorizationResponse(TransactionEvent transactionEvent, Amount amount, AuthorizationStatus authorizationStatus) {
        setUserId(transactionEvent.getUserId());
        setMessageId(transactionEvent.getMessageId());
        setResponseCode(authorizationStatus);
        setBalance(amount);
    }
}
