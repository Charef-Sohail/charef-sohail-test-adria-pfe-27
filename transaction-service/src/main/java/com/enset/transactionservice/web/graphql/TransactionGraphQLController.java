package com.enset.transactionservice.web.graphql;

import com.enset.transactionservice.domain.Transaction;
import com.enset.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TransactionGraphQLController {

    private final TransactionService transactionService;

    @QueryMapping
    public List<Transaction> transactions() {
        return transactionService.getAllTransactions();
    }

    @QueryMapping
    public List<Transaction> transactionsByAccount(@Argument UUID accountId) {
        return transactionService.getTransactionsByAccount(accountId);
    }

    @MutationMapping
    public Transaction executeTransaction(@Argument TransactionInput input) {
        Transaction tx = Transaction.builder()
                .accountId(input.accountId())
                .amount(BigDecimal.valueOf(input.amount()))
                .currency(input.currency())
                .merchantCategory(input.merchantCategory())
                .country(input.country())
                .build();
        return transactionService.executeTransaction(tx);
    }

    public record TransactionInput(UUID accountId, double amount, String currency, String merchantCategory, String country) {}
}