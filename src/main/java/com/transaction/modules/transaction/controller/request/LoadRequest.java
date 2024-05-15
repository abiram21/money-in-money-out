package com.transaction.modules.transaction.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
public class LoadRequest {

    @NotEmpty(message = "User Id cannot be empty.")
    private String userId;

    @NotEmpty(message = "Message Id cannot be empty.")
    private String messageId;

    @Valid
    @NotNull(message = "Transaction amount is required.")
    private Amount transactionAmount;
}
