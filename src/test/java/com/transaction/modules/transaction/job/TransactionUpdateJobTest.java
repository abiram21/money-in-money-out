package com.transaction.modules.transaction.job;

import com.transaction.modules.account.entity.Account;
import com.transaction.modules.account.repository.AccountRepository;
import com.transaction.modules.transaction.entity.Transaction;
import com.transaction.modules.transaction.entity.TransactionEvent;
import com.transaction.modules.transaction.enums.TransactionEventStatus;
import com.transaction.modules.transaction.enums.TransactionType;
import com.transaction.modules.transaction.job.TransactionUpdateJob;
import com.transaction.modules.transaction.repository.TransactionEventRepository;
import com.transaction.modules.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.annotation.EnableRetry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@EnableRetry
class TransactionUpdateJobTest {
    @Mock
    private TransactionEventRepository transactionEventRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionUpdateJob transactionUpdateJob;

    @Test
    void testExecute() {
        // Prepare test data
        List<TransactionEvent> pendingTransactionEventList = new ArrayList<>();
        TransactionEvent transactionEvent1 = new TransactionEvent();
        transactionEvent1.setStatus(TransactionEventStatus.PENDING);
        transactionEvent1.setRunningBalance(200.0);
        transactionEvent1.setCurrency("$");
        transactionEvent1.setType(TransactionType.CREDIT);
        transactionEvent1.setAmount(200.0);
        pendingTransactionEventList.add(transactionEvent1);

        TransactionEvent transactionEvent2 = new TransactionEvent();
        transactionEvent2.setStatus(TransactionEventStatus.PENDING);
        transactionEvent2.setRunningBalance(200.0);
        transactionEvent2.setCurrency("$");
        transactionEvent2.setType(TransactionType.DEBIT);
        transactionEvent2.setAmount(100.0);
        pendingTransactionEventList.add(transactionEvent2);


        when(transactionEventRepository.findByStatus(TransactionEventStatus.PENDING))
                .thenReturn(pendingTransactionEventList);
        when(accountRepository.findByUserId(transactionEvent1.getUserId()))
                .thenReturn(new Account());

        // Execute the method
        transactionUpdateJob.execute();

        // Verify that save methods are called
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionEventRepository, times(2)).save(any(TransactionEvent.class));
    }

    @Test
    void testExecuteWithError() {
        when(transactionEventRepository.findByStatus(TransactionEventStatus.PENDING)).thenThrow(RuntimeException.class);
        assertThrows(RuntimeException.class, () -> transactionUpdateJob.execute());
    }
}