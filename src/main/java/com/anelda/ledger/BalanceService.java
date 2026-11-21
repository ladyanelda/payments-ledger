package com.anelda.ledger;

import org.springframework.stereotype.Service;

@Service
public class BalanceService {

    private final AccountRepository accounts;
    private final EntryRepository entries;

    public BalanceService(AccountRepository accounts, EntryRepository entries) {
        this.accounts = accounts;
        this.entries = entries;
    }

    public long balanceOf(Long accountId) {
        Account account = accounts.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("No account " + accountId));
        long debits = entries.sumFor(accountId, Direction.DEBIT);
        long credits = entries.sumFor(accountId, Direction.CREDIT);
        if (account.getType() == AccountType.ASSET) {
            return debits - credits;
        }
        return credits - debits;
    }
}
