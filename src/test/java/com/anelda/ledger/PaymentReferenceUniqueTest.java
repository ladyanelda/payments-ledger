package com.anelda.ledger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class PaymentReferenceUniqueTest {

    @Autowired PaymentRepository payments;

    @Test
    void sameReferenceCannotBeSavedTwice() {
        String ref = "ref_" + System.nanoTime();
        payments.saveAndFlush(new Payment(ref, 2000));

        assertThatThrownBy(() -> payments.saveAndFlush(new Payment(ref, 2000)))
            .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(payments.findByPaystackReference(ref)).isPresent();
    }
}
