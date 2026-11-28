package com.anelda.ledger;

import org.junit.jupiter.api.Test;

import static com.anelda.ledger.PaymentStatus.*;
import static org.assertj.core.api.Assertions.*;

class PaymentStatusTest {

    @Test
    void allowedMoves() {
        assertThat(INITIATED.canMoveTo(PENDING)).isTrue();
        assertThat(PENDING.canMoveTo(SUCCESSFUL)).isTrue();
        assertThat(PENDING.canMoveTo(FAILED)).isTrue();
        assertThat(SUCCESSFUL.canMoveTo(REFUNDED)).isTrue();
    }

    @Test
    void cannotRefundAFailedPayment() {
        assertThat(FAILED.canMoveTo(REFUNDED)).isFalse();
    }

    @Test
    void failedPaymentCannotBecomeSuccessful() {
        assertThat(FAILED.canMoveTo(SUCCESSFUL)).isFalse();
    }

    @Test
    void refundedPaymentCannotBecomeSuccessful() {
        assertThat(REFUNDED.canMoveTo(SUCCESSFUL)).isFalse();
    }

    @Test
    void successfulPaymentCannotFail() {
        assertThat(SUCCESSFUL.canMoveTo(FAILED)).isFalse();
    }

    @Test
    void finalStatesCannotMoveAnywhere() {
        for (PaymentStatus next : PaymentStatus.values()) {
            assertThat(FAILED.canMoveTo(next)).isFalse();
            assertThat(REFUNDED.canMoveTo(next)).isFalse();
        }
    }
}