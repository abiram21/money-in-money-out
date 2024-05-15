package com.transaction.modules.transaction.repository;

import com.transaction.modules.transaction.entity.TransactionEvent;
import com.transaction.modules.transaction.enums.TransactionEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionEventRepository extends JpaRepository<TransactionEvent, String> {

    List<TransactionEvent> findByStatus(TransactionEventStatus status);

    // To obtain the updated balance from the transaction event table
    @Query("SELECT runningBalance FROM TransactionEvent WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    Double getLatestRunningBalanceByUserId(@Param("userId") String userId);

}
