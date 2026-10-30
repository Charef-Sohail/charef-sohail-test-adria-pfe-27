import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface Account {
  id: string;
  customerReference: string;
  status: string;
  currency: string;
  balance: number;
}

interface TransactionForm {
  accountId: string;
  amount: number | null;
  currency: string;
  merchantCategory: string;
  country: string;
}

@Component({
  imports: [CommonModule, FormsModule],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080';

  accounts: Account[] = [];
  loading = false;
  saving = false;
  error = '';
  message = '';
  maxAmount = 1000;
  transaction: TransactionForm = this.newTransaction();
  transfer = { sourceAccountId: '', destAccountId: '', amount: null as number | null };

  get totalBalance(): number {
    return this.accounts.reduce((total, account) => total + account.balance, 0);
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.error = '';
    this.http.get<Account[]>(`${this.apiUrl}/account-service/api/accounts`).subscribe({
      next: (accounts) => {
        this.accounts = accounts;
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger les comptes. Vérifiez que le gateway et account-service sont démarrés.';
        this.loading = false;
      },
    });
  }

  createTransaction(): void {
    this.clearFeedback();
    const account = this.accounts.find((item) => item.id === this.transaction.accountId);
    if (!account || account.status.toUpperCase() !== 'ACTIVE') {
      this.error = 'Le compte sélectionné doit exister et être actif.';
      return;
    }
    if (!this.transaction.amount || this.transaction.amount <= 0 || this.transaction.amount > this.maxAmount) {
      this.error = `Le montant doit être positif et inférieur ou égal à ${this.maxAmount} €.`;
      return;
    }
    if (account.currency.toUpperCase() !== this.transaction.currency.toUpperCase()) {
      this.error = `La devise doit être ${account.currency} pour ce compte.`;
      return;
    }
    if (account.balance < this.transaction.amount) {
      this.error = 'Le solde du compte est insuffisant.';
      return;
    }

    this.saving = true;
    this.http.post(`${this.apiUrl}/transaction-service/api/transactions`, this.transaction).subscribe({
      next: () => {
        this.message = 'Transaction enregistrée et solde mis à jour.';
        this.saving = false;
        this.transaction = this.newTransaction();
        this.loadData();
      },
      error: () => {
        this.error = 'La transaction n’a pas pu être enregistrée. Consultez les logs du service.';
        this.saving = false;
      },
    });
  }

  makeTransfer(): void {
    this.clearFeedback();
    const { sourceAccountId, destAccountId, amount } = this.transfer;
    if (sourceAccountId === destAccountId) {
      this.error = 'Les comptes source et destinataire doivent être différents.';
      return;
    }
    if (!amount || amount <= 0) {
      this.error = 'Le montant du virement doit être strictement positif.';
      return;
    }
    const source = this.accounts.find((account) => account.id === sourceAccountId);
    if (!source || source.status.toUpperCase() !== 'ACTIVE') {
      this.error = 'Le compte source doit exister et être actif.';
      return;
    }
    if (source.balance < amount) {
      this.error = 'Le solde du compte source est insuffisant.';
      return;
    }

    this.saving = true;
    const params = { sourceAccountId, destAccountId, amount: String(amount) };
    this.http.post(`${this.apiUrl}/transaction-service/api/transactions/transfer`, null, { params }).subscribe({
      next: () => {
        this.message = 'Virement effectué avec succès.';
        this.saving = false;
        this.transfer = { sourceAccountId: '', destAccountId: '', amount: null };
        this.loadData();
      },
      error: () => {
        this.error = 'Le virement n’a pas pu être effectué.';
        this.saving = false;
      },
    });
  }

  private newTransaction(): TransactionForm {
    return { accountId: '', amount: null, currency: 'EUR', merchantCategory: '', country: 'MA' };
  }

  private clearFeedback(): void {
    this.error = '';
    this.message = '';
  }
}
