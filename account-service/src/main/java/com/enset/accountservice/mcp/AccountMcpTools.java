package com.enset.accountservice.mcp;

import com.enset.accountservice.domain.Account;
import com.enset.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.UUID;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
public class AccountMcpTools {

    private final AccountService accountService;

    public record AccountRequest(UUID accountId) {}

    @Bean
    @Description("Permet de récupérer les informations et le solde d'un compte bancaire à partir de son ID")
    public Function<AccountRequest, Account> getAccountDetails() {
        return request -> accountService.getAccountById(request.accountId());
    }
}