package com.enset.accountservice.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "bank_accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String customerReference;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    private String currency;
    private double balance;
}