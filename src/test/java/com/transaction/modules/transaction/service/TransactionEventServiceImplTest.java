package com.transaction.modules.transaction.service;

import com.transaction.modules.account.entity.Account;
import com.transaction.modules.account.repository.AccountRepository;
import com.transaction.modules.transaction.controller.request.Amount;
import com.transaction.modules.transaction.controller.request.AuthorizationRequest;
import com.transaction.modules.transaction.controller.request.LoadRequest;
import com.transaction.modules.transaction.controller.response.AuthorizationResponse;
import com.transaction.modules.transaction.controller.response.LoadResponse;
import com.transaction.modules.transaction.entity.TransactionEvent;
import com.transaction.modules.transaction.enums.TransactionEventStatus;
import com.transaction.modules.transaction.enums.TransactionType;
import com.transaction.modules.transaction.job.TransactionUpdateJob;
import com.transaction.modules.transaction.repository.TransactionEventRepository;
import com.transaction.modules.transaction.service.TransactionEventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionEventServiceImplTest {

    private final String messageId = "testMessageId";
    private final String userId = "testUser";
    @Mock
    private TransactionEventRepository transactionEventRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionUpdateJob transactionUpdateJob;
    @InjectMocks
    private TransactionEventServiceImpl transactionEventService;
    private LoadRequest loadRequest;
    private AuthorizationRequest authorizationRequest;

    @BeforeEach
    void setUp() {

        loadRequest = new LoadRequest();
        loadRequest.setUserId(userId);
        loadRequest.setMessageId(messageId);

        Amount loadAmount = new Amount();
        loadAmount.setDebitOrCredit(TransactionType.CREDIT);
        loadAmount.setAmount("300.00");
        loadAmount.setCurrency("$");
        loadRequest.setTransactionAmount(loadAmount);

        authorizationRequest = new AuthorizationRequest();
        authorizationRequest.setUserId(userId);
        authorizationRequest.setMessageId(messageId);
        Amount authoriseAmount = new Amount();
        authoriseAmount.setDebitOrCredit(TransactionType.DEBIT);
        authoriseAmount.setAmount("300.00");
        authoriseAmount.setCurrency("$");
        authorizationRequest.setTransactionAmount(authoriseAmount);
    }

    // Credits the amount to balance successfully
    @Test
    void addCreditAmount() {
        when(transactionEventRepository.getLatestRunningBalanceByUserId(loadRequest.getUserId())).thenReturn(50.0);
        TransactionEvent result = transactionEventService.addCreditAmount(this.loadRequest);
        assertEquals(350.0, result.getRunningBalance());
    }

    // Debits the amount to balance successfully
    @Test
    void deductDebitAmount() {
        TransactionEvent result = transactionEventService.deductDebitAmount(authorizationRequest, 600.0, TransactionEventStatus.PENDING);
        assertEquals(300.0, result.getRunningBalance());
    }

    // Loads amount successfully
    @Test
    void testLoadSuccess() {

        TransactionEvent transactionEvent = new TransactionEvent(loadRequest, 500.00);
        when(transactionEventRepository.findById(loadRequest.getMessageId())).thenReturn(Optional.empty());
        when(accountRepository.findById(loadRequest.getUserId())).thenReturn(Optional.of(new Account()));
        when(transactionEventRepository.save(any(TransactionEvent.class))).thenReturn(transactionEvent);
        when(transactionEventRepository.getLatestRunningBalanceByUserId(loadRequest.getUserId())).thenReturn(200.00);

        LoadResponse loadResponse = transactionEventService.load(loadRequest, "testMessageId");

        assertEquals(loadResponse.getMessageId(), messageId);
        assertEquals(loadResponse.getUserId(), userId);
        assertEquals(loadResponse.getBalance().getAmount(), "500.0");
        verify(transactionEventRepository).save(any(TransactionEvent.class));
        await().untilAsserted(() -> {
            // Verify that execute method was called asynchronously
            verify(transactionUpdateJob, times(1)).execute();
        });
    }

    // Loads throws error if there is a existing message id
    @Test
    void testLoadErrorExistingMessageId() {
        when(transactionEventRepository.findById(loadRequest.getMessageId())).thenReturn(Optional.of(new TransactionEvent()));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionEventService.load(loadRequest, messageId);
        });
        assertEquals("Duplicate message id " + messageId, exception.getMessage());
    }

    // Loads throws error if there is a message id mismatch in the path and body.
    @Test
    void testLoadErrorNotSameMessageId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionEventService.load(loadRequest, "invalidMessageId");
        });
        assertEquals("Message ids are not matched", exception.getMessage());
    }

    // Loads throws error if the type is not CREDIT.
    @Test
    void testLoadErrorDebitType() {
        loadRequest.getTransactionAmount().setDebitOrCredit(TransactionType.DEBIT);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionEventService.load(loadRequest, messageId);
        });
        assertEquals("Request type must be CREDIT", exception.getMessage());
    }

    // Loads throws error if user is not found
    @Test
    void testLoadErrorNoUserFound() {
        when(accountRepository.findById(loadRequest.getUserId())).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionEventService.load(loadRequest, messageId);
        });
        assertEquals("User not found for user id " + userId, exception.getMessage());
    }

    // Authorize amount successfully
    @Test
    void testAuthorizeSuccess() {

        TransactionEvent transactionEvent = new TransactionEvent(authorizationRequest, 500.00, TransactionEventStatus.PENDING);

        when(accountRepository.findById(authorizationRequest.getUserId())).thenReturn(Optional.of(new Account()));
        when(transactionEventRepository.save(any(TransactionEvent.class))).thenReturn(transactionEvent);
        when(transactionEventRepository.getLatestRunningBalanceByUserId(authorizationRequest.getUserId())).thenReturn(500.00);

        AuthorizationResponse authorizationResponse = transactionEventService.authorize(authorizationRequest, "testMessageId");

        assertEquals(authorizationResponse.getMessageId(), messageId);
        assertEquals(authorizationResponse.getBalance().getAmount(), "200.0");
        verify(transactionEventRepository).save(any(TransactionEvent.class));
        await().untilAsserted(() -> {
            // Verify that execute method was called asynchronously
            verify(transactionUpdateJob, times(1)).execute();
        });
    }

    // Authorize throws error if balance is insufficient.
    @Test
    void testAuthorizeFailedSuccess() {

        TransactionEvent transactionEvent = new TransactionEvent(authorizationRequest, 200.00, TransactionEventStatus.PENDING);

        when(accountRepository.findById(authorizationRequest.getUserId())).thenReturn(Optional.of(new Account()));
        when(transactionEventRepository.save(any(TransactionEvent.class))).thenReturn(transactionEvent);
        when(transactionEventRepository.getLatestRunningBalanceByUserId(authorizationRequest.getUserId())).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionEventService.authorize(authorizationRequest, messageId);
        });
        assertEquals("Insufficient balance", exception.getMessage());
    }

    // Authorize throws error if there is a existing message id
    @Test
    void testAuthoriseErrorExistingMessageId() {
        when(transactionEventRepository.findById(authorizationRequest.getMessageId())).thenReturn(Optional.of(new TransactionEvent()));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionEventService.authorize(authorizationRequest, messageId);
        });
        assertEquals("Duplicate message id " + messageId, exception.getMessage());
    }

    // Authorize throws error if there is a message id mismatch in the path and body.
    @Test
    void testAuthoriseErrorNotSameMessageId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionEventService.authorize(authorizationRequest, "invalidMessageId");
        });
        assertEquals("Message ids are not matched", exception.getMessage());
    }

    // Authorize throws error if the type is not DEBIT.
    @Test
    void testAuthoriseErrorDebitType() {
        authorizationRequest.getTransactionAmount().setDebitOrCredit(TransactionType.CREDIT);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionEventService.authorize(authorizationRequest, messageId);
        });
        assertEquals("Request type must be DEBIT", exception.getMessage());
    }

    // Authorize throws error if user is not found
    @Test
    void testAuthoriseErrorNoUserFound() {
        when(accountRepository.findById(authorizationRequest.getUserId())).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionEventService.authorize(authorizationRequest, messageId);
        });
        assertEquals("User not found for user id " + userId, exception.getMessage());
    }
}