package com.enset.transactionservice.service;

import com.enset.transactionservice.domain.Transaction;
import com.enset.transactionservice.feign.AccountRestClient;
import com.enset.transactionservice.feign.model.AccountDTO;
import com.enset.transactionservice.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRestClient accountRestClient;

    @Value("${bank.transaction.max-amount:1000}")
    private double maxAmount;

    @Override
    public Transaction executeTransaction(Transaction transaction) {
        // Règle 1 : Montant positif et respect du plafond
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement positif.");
        }
        if (transaction.getAmount().doubleValue() > maxAmount) {
            throw new IllegalArgumentException("Le montant dépasse le plafond configuré de : " + maxAmount);
        }

        // Règle 2 : Le compte doit exister et être ACTIVE
        AccountDTO account = accountRestClient.getAccountById(transaction.getAccountId());
        if (account == null || !"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new IllegalStateException("Le compte n'existe pas ou n'est pas actif.");
        }

        // Règle 3 : Correspondance de devise
        if (!account.getCurrency().equalsIgnoreCase(transaction.getCurrency())) {
            throw new IllegalArgumentException("La devise de la transaction (" + transaction.getCurrency()
                    + ") ne correspond pas à celle du compte (" + account.getCurrency() + ").");
        }

        // Règle 4 : Solde suffisant & Mise à jour après débit
        if (account.getBalance() < transaction.getAmount().doubleValue()) {
            throw new IllegalStateException("Solde insuffisant. Solde actuel : " + account.getBalance());
        }

        accountRestClient.updateBalance(account.getId(), -transaction.getAmount().doubleValue());

        transaction.setCreatedAt(Instant.now());
        return transactionRepository.save(transaction);
    }

    @Override
    public void transfer(UUID sourceAccountId, UUID destAccountId, BigDecimal amount) {
        // Débit source
        Transaction debit = Transaction.builder()
                .accountId(sourceAccountId)
                .amount(amount)
                .currency("EUR")
                .merchantCategory("TRANSFER_OUT")
                .country("MA")
                .build();
        executeTransaction(debit);

        // Crédit destination
        accountRestClient.updateBalance(destAccountId, amount.doubleValue());

        Transaction credit = Transaction.builder()
                .accountId(destAccountId)
                .amount(amount)
                .currency("EUR")
                .merchantCategory("TRANSFER_IN")
                .country("MA")
                .createdAt(Instant.now())
                .build();
        transactionRepository.save(credit);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public List<Transaction> getTransactionsByAccount(UUID accountId) {
        return transactionRepository.findByAccountId(accountId);
    }
}
