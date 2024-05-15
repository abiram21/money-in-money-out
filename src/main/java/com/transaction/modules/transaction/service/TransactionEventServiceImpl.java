package com.transaction.modules.transaction.service;

import com.transaction.modules.account.repository.AccountRepository;
import com.transaction.modules.transaction.controller.request.Amount;
import com.transaction.modules.transaction.controller.request.AuthorizationRequest;
import com.transaction.modules.transaction.controller.request.LoadRequest;
import com.transaction.modules.transaction.controller.response.AuthorizationResponse;
import com.transaction.modules.transaction.controller.response.LoadResponse;
import com.transaction.modules.transaction.entity.TransactionEvent;
import com.transaction.modules.transaction.enums.AuthorizationStatus;
import com.transaction.modules.transaction.enums.TransactionEventStatus;
import com.transaction.modules.transaction.enums.TransactionType;
import com.transaction.modules.transaction.job.TransactionUpdateJob;
import com.transaction.modules.transaction.repository.TransactionEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class TransactionEventServiceImpl implements TransactionEventService {

    @Autowired
    private TransactionEventRepository transactionEventRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionUpdateJob transactionUpdateJob;

    /**
     * Adds credit amount to the user's balance based on the provided load request.
     */
    public TransactionEvent addCreditAmount(LoadRequest loadRequest) {
        Double balance = this.findCurrentBalance(loadRequest.getUserId());
        balance += Double.parseDouble(loadRequest.getTransactionAmount().getAmount());
        return new TransactionEvent(loadRequest, balance);
    }

    /**
     * Deducts debit amount from the user's balance for authorization request with the given balance and transaction event status.
     */
    public TransactionEvent deductDebitAmount
    (AuthorizationRequest authorizationRequest, Double balance, TransactionEventStatus transactionEventStatus) {
        if (transactionEventStatus == TransactionEventStatus.PENDING) {
            balance -= Double.parseDouble(authorizationRequest.getTransactionAmount().getAmount());
        }
        return new TransactionEvent(authorizationRequest, balance, transactionEventStatus);
    }

    /**
     * Finds the current balance of the user identified by the provided user ID.
     */
    public Double findCurrentBalance(String userId) {
        Double balance = transactionEventRepository.getLatestRunningBalanceByUserId(userId);
        if (balance == null) {
            balance = 0.0;
        }
        return balance;
    }

    /**
     * Loads money into the account based on the provided load request
     * It will start a job separately to update the transaction and account balance to be run in background
     */
    @Override
    public LoadResponse load(LoadRequest loadRequest, String messageId) {
        validateMessageId(loadRequest.getMessageId(), messageId);
        validateTransactionType(loadRequest.getTransactionAmount().getDebitOrCredit(), TransactionType.CREDIT);
        validateAccount(loadRequest.getUserId());

        TransactionEvent transactionEvent = addCreditAmount(loadRequest);
        transactionEventRepository.save(transactionEvent);

        // Starts a separate thread to run in background
        transactionUpdateJob.execute();

        Amount amount = new Amount(transactionEvent);
        return new LoadResponse(transactionEvent, amount);
    }

    /**
     * Authorizes a transaction based on the provided authorization request and message ID.
     * It will start a job separately to update the transaction and account balance to be run in background
     */
    @Override
    public AuthorizationResponse authorize(AuthorizationRequest authorizationRequest, String messageId) {
        validateMessageId(authorizationRequest.getMessageId(), messageId);
        validateTransactionType(authorizationRequest.getTransactionAmount().getDebitOrCredit(), TransactionType.DEBIT);
        validateAccount(authorizationRequest.getUserId());

        Double balance = findCurrentBalance(authorizationRequest.getUserId());
        if (balance < Double.parseDouble(authorizationRequest.getTransactionAmount().getAmount())) {
            TransactionEvent failedEvent = deductDebitAmount(authorizationRequest, balance, TransactionEventStatus.FAILED);
            transactionEventRepository.save(failedEvent);
            throw new IllegalArgumentException("Insufficient balance");
        }

        TransactionEvent pendingEvent = deductDebitAmount(authorizationRequest, balance, TransactionEventStatus.PENDING);
        transactionEventRepository.save(pendingEvent);

        // Starts a separate thread to run in background
        transactionUpdateJob.execute();

        Amount amount = new Amount(pendingEvent);
        return new AuthorizationResponse(pendingEvent, amount, AuthorizationStatus.APPROVED);
    }

    /**
     * Validates the provided message ID against the provided message ID and checks for duplicate message IDs in the repository.
     */
    private void validateMessageId(String requestMessageId, String providedMessageId) {
        if (!Objects.equals(requestMessageId, providedMessageId)) {
            throw new IllegalArgumentException("Message ids are not matched");
        }
        Optional<TransactionEvent> existingTransaction = transactionEventRepository.findById(requestMessageId);
        if (existingTransaction.isPresent()) {
            throw new IllegalArgumentException("Duplicate message id " + requestMessageId);
        }
    }

    /**
     * Validates that the transaction type matches the expected type.
     */
    private void validateTransactionType(TransactionType requestType, TransactionType expectedType) {
        if (requestType != expectedType) {
            throw new IllegalArgumentException("Request type must be " + expectedType);
        }
    }

    /**
     * Validates the existence of the user account based on the provided user ID.
     */
    private void validateAccount(String userId) {
        accountRepository.findById(userId).
                orElseThrow(() -> new IllegalArgumentException("User not found for user id " + userId));
    }

}
