package com.anelda.ledger;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PaymentTest {

    @Test
    void newPaymentStartsInitiated() {
        Payment p = new Payment("ref_new", 2000);
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.INITIATED);
    }

    @Test
    void followsTheHappyPath() {
        Payment p = new Payment("ref_happy", 2000);
        p.moveTo(PaymentStatus.PENDING);
        p.moveTo(PaymentStatus.SUCCESSFUL);
        p.moveTo(PaymentStatus.REFUNDED);
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void invalidMoveIsRefusedAndStatusStaysTheSame() {
        Payment p = new Payment("ref_failed", 2000);
        p.moveTo(PaymentStatus.PENDING);
        p.moveTo(PaymentStatus.FAILED);

        assertThatThrownBy(() -> p.moveTo(PaymentStatus.REFUNDED))
            .isInstanceOf(IllegalStateException.class);
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }
}
