package com.enset.accountservice.service;

import com.enset.accountservice.domain.Account;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    Account createAccount(Account account);
    List<Account> getAllAccounts();
    Account getAccountById(UUID id);
    Account updateBalance(UUID id, double amount);
}