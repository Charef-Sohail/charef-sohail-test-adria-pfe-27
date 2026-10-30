package com.enset.transactionservice.mcp;


import com.enset.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionMcpTools {

    private final TransactionService transactionService;

    @Tool(description = "Exécuter un virement entre deux comptes bancaires via les UUID source et destination")
    public String executeTransfer(UUID sourceAccountId, UUID destAccountId, double amount) {
        transactionService.transfer(sourceAccountId, destAccountId, BigDecimal.valueOf(amount));
        return "Virement de " + amount + " EUR validé du compte " + sourceAccountId + " vers " + destAccountId;
    }
}