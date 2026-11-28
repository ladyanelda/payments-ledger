package com.anelda.ledger;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paystack_reference", nullable = false, unique = true, columnDefinition = "text")
    private String paystackReference;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "text")
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Payment() {}

    public Payment(String paystackReference, long amount) {
        this.paystackReference = paystackReference;
        this.amount = amount;
        this.status = PaymentStatus.INITIATED;
        this.createdAt = Instant.now();
    }

    public void moveTo(PaymentStatus next) {
        if (!status.canMoveTo(next)) {
            throw new IllegalStateException(
                "Cannot move payment " + paystackReference + " from " + status + " to " + next);
        }
        this.status = next;
    }

    public Long getId() { return id; }
    public String getPaystackReference() { return paystackReference; }
    public long getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
}
