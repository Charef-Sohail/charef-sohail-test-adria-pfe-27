package com.enset.transactionservice.feign;

import com.enset.transactionservice.feign.model.AccountDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "account-service")
public interface AccountRestClient {

    @GetMapping("/api/accounts/{id}")
    @CircuitBreaker(name = "accountService", fallbackMethod = "getAccountFallback")
    AccountDTO getAccountById(@PathVariable("id") UUID id);

    @PutMapping("/api/accounts/{id}/update-balance")
    @CircuitBreaker(name = "accountService", fallbackMethod = "updateBalanceFallback")
    AccountDTO updateBalance(@PathVariable("id") UUID id, @RequestParam("amount") double amount);

    default AccountDTO getAccountFallback(UUID id, Throwable exception) {
        AccountDTO fallback = new AccountDTO();
        fallback.setId(id);
        fallback.setStatus("BLOCKED");
        return fallback;
    }

    default AccountDTO updateBalanceFallback(UUID id, double amount, Throwable exception) {
        throw new RuntimeException("Service account-service indisponible via Circuit Breaker : " + exception.getMessage());
    }
}