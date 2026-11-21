package com.anelda.ledger;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions")
public class LedgerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL)
    private List<Entry> entries = new ArrayList<>();

    protected LedgerTransaction() {}

    public LedgerTransaction(String description) {
        this.description = description;
        this.createdAt = Instant.now();
    }

    public void addEntry(Account account, Direction direction, long amount) {
        entries.add(new Entry(this, account, direction, amount));
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Entry> getEntries() { return entries; }
}
