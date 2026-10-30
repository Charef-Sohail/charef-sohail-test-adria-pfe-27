package com.enset.accountservice.web.graphql;

import com.enset.accountservice.domain.Account;
import com.enset.accountservice.domain.AccountStatus;
import com.enset.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AccountGraphQLController {

    private final AccountService accountService;

    @QueryMapping
    public List<Account> accounts() {
        return accountService.getAllAccounts();
    }

    @QueryMapping
    public Account accountById(@Argument UUID id) {
        return accountService.getAccountById(id);
    }

    @MutationMapping
    public Account createAccount(@Argument AccountInput accountInput) {
        Account account = Account.builder()
                .customerReference(accountInput.customerReference())
                .currency(accountInput.currency())
                .balance(accountInput.balance())
                .status(AccountStatus.ACTIVE)
                .build();
        return accountService.createAccount(account);
    }

    public record AccountInput(String customerReference, String currency, double balance) {}
}