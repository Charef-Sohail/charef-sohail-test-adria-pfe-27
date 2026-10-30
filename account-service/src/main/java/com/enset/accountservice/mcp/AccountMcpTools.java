package com.enset.accountservice.mcp;

import com.enset.accountservice.domain.Account;
import com.enset.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class AccountMcpTools {

    private final AccountService accountService;

    @Tool(description = "Récupérer les détails et le solde d'un compte bancaire à partir de son identifiant UUID")
    public Account getAccountDetails(UUID accountId) {
        return accountService.getAccountById(accountId);
    }
}