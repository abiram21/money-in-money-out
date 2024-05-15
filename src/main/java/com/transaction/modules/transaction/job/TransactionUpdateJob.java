package com.transaction.modules.transaction.job;

import com.transaction.modules.account.entity.Account;
import com.transaction.modules.account.repository.AccountRepository;
import com.transaction.modules.transaction.entity.Transaction;
import com.transaction.modules.transaction.entity.TransactionEvent;
import com.transaction.modules.transaction.enums.TransactionEventStatus;
import com.transaction.modules.transaction.repository.TransactionEventRepository;
import com.transaction.modules.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionUpdateJob {

    @Autowired
    private TransactionEventRepository transactionEventRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    private static final Logger logger = LoggerFactory.getLogger(TransactionUpdateJob.class);

    /*
     * Creates a separate thread to run in background
     * Transactional for data consistency
     */
    @Async
    @Retryable(retryFor = {RuntimeException.class}, maxAttempts = 2, backoff = @Backoff(delay = 2000))
    @Transactional(rollbackFor = Exception.class)
    public synchronized void execute() {
        try {

            List<TransactionEvent> pendingTransactionEventList = transactionEventRepository.findByStatus(TransactionEventStatus.PENDING);
            pendingTransactionEventList.forEach(transactionEvent -> {
                Transaction transaction = new Transaction(transactionEvent);
                transactionRepository.save(transaction);
                Account account = accountRepository.findByUserId(transactionEvent.getUserId());
                account.setBalance(transactionEvent.getRunningBalance());
                accountRepository.save(account);
                transactionEvent.setStatus(TransactionEventStatus.PROCESSED);
                transactionEventRepository.save(transactionEvent);
            });
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }
}