package com.enset.accountservice;

import com.enset.accountservice.domain.Account;
import com.enset.accountservice.domain.AccountStatus;
import com.enset.accountservice.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner start(AccountRepository accountRepository) {
        return args -> {
            accountRepository.save(Account.builder()
                    .customerReference("CUST-001")
                    .currency("EUR")
                    .balance(5000.0)
                    .status(AccountStatus.ACTIVE)
                    .build());
            accountRepository.save(Account.builder()
                    .customerReference("CUST-002")
                    .currency("EUR")
                    .balance(3500.0)
                    .status(AccountStatus.ACTIVE)
                    .build());
        };
    }
}
