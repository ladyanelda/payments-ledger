package com.anelda.ledger;

public enum PaymentStatus {
    INITIATED,
    PENDING,
    SUCCESSFUL,
    FAILED,
    REFUNDED;

    public boolean canMoveTo(PaymentStatus next) {
        return switch (this) {
            case INITIATED -> next == PENDING;
            case PENDING -> next == SUCCESSFUL || next == FAILED;
            case SUCCESSFUL -> next == REFUNDED;
            case FAILED, REFUNDED -> false;
        };
    }
}
