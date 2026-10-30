package com.enset.transactionservice.service;

import com.enset.transactionservice.domain.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionService {
    Transaction executeTransaction(Transaction transaction);
    void transfer(UUID sourceAccountId, UUID destAccountId, BigDecimal amount);
    List<Transaction> getAllTransactions();
    List<Transaction> getTransactionsByAccount(UUID accountId);
}