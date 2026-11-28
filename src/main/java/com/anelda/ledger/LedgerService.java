package com.anelda.ledger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class LedgerService {

    public record Line(Long accountId, Direction direction, long amount) {}

    private final AccountRepository accounts;
    private final LedgerTransactionRepository transactions;

    public LedgerService(AccountRepository accounts, LedgerTransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional
    public LedgerTransaction post(String description, List<Line> lines) {
        return post(description, null, lines);
    }

    @Transactional
    public LedgerTransaction post(String description, String sourceReference, List<Line> lines) {
        if (lines.size() < 2) {
            throw new IllegalArgumentException("A transaction needs at least two entries");
        }
        long debits = 0;
        long credits = 0;
        for (Line line : lines) {
            if (line.amount() <= 0) {
                throw new IllegalArgumentException("Amounts must be positive");
            }
            if (line.direction() == Direction.DEBIT) debits += line.amount();
            else credits += line.amount();
        }
        if (debits != credits) {
            throw new IllegalArgumentException("Unbalanced: debits " + debits + ", credits " + credits);
        }
        LedgerTransaction tx = new LedgerTransaction(description, sourceReference);
        for (Line line : lines) {
            Account account = accounts.findById(line.accountId())
                .orElseThrow(() -> new IllegalArgumentException("No account " + line.accountId()));
            tx.addEntry(account, line.direction(), line.amount());
        }
        return transactions.saveAndFlush(tx);
    }
}
