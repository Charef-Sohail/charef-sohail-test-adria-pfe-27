package com.enset.transactionservice.web.rest;

import com.enset.transactionservice.domain.Transaction;
import com.enset.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionRestController {

    private final TransactionService transactionService;

    @GetMapping
    public List<Transaction> getAll() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/account/{accountId}")
    public List<Transaction> getByAccount(@PathVariable UUID accountId) {
        return transactionService.getTransactionsByAccount(accountId);
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.executeTransaction(transaction));
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @RequestParam UUID sourceAccountId,
            @RequestParam UUID destAccountId,
            @RequestParam BigDecimal amount) {
        transactionService.transfer(sourceAccountId, destAccountId, amount);
        return ResponseEntity.ok("Virement de " + amount + " effectué avec succès.");
    }
}