package com.anelda.ledger;

import jakarta.persistence.*;

@Entity
@Table(name = "entries")
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "transaction_id")
    private LedgerTransaction transaction;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "text")
    private Direction direction;

    @Column(nullable = false)
    private long amount;

    protected Entry() {}

    Entry(LedgerTransaction transaction, Account account, Direction direction, long amount) {
        this.transaction = transaction;
        this.account = account;
        this.direction = direction;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public Account getAccount() { return account; }
    public Direction getDirection() { return direction; }
    public long getAmount() { return amount; }
}
