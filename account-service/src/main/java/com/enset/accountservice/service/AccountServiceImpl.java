package com.enset.accountservice.service;

import com.enset.accountservice.domain.Account;
import com.enset.accountservice.domain.AccountStatus;
import com.enset.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public Account createAccount(Account account) {
        if (account.getStatus() == null) {
            account.setStatus(AccountStatus.ACTIVE);
        }
        return accountRepository.save(account);
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Account getAccountById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé : " + id));
    }

    @Override
    public Account updateBalance(UUID id, double amount) {
        Account account = getAccountById(id);
        account.setBalance(account.getBalance() + amount);
        return accountRepository.save(account);
    }
}