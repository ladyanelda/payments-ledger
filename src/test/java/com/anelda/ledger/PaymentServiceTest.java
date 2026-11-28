package com.anelda.ledger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class PaymentServiceTest {

    @Autowired PaymentService service;
    @Autowired PaymentRepository payments;
    @Autowired AccountRepository accounts;
    @Autowired LedgerService ledger;
    @Autowired JdbcTemplate jdbc;

    private String pendingPayment(long amount) {
        String ref = "ref_" + System.nanoTime();
        Payment p = new Payment(ref, amount);
        p.moveTo(PaymentStatus.PENDING);
        payments.save(p);
        return ref;
    }

    @Test
    void sameWebhookTwiceCreatesOnlyOneLedgerTransaction() {
        String ref = pendingPayment(2000);

        service.handleSuccess(ref);
        service.handleSuccess(ref);

        Integer count = jdbc.queryForObject(
            "select count(*) from transactions where source_reference = ?",
            Integer.class, "payment:" + ref + ":success");
        assertThat(count).isEqualTo(1);
        assertThat(payments.findByPaystackReference(ref).get().getStatus())
            .isEqualTo(PaymentStatus.SUCCESSFUL);
    }

    @Test
    void feeRoundsToNearestAndSharesAlwaysAddUp() {
        assertThat(PaymentService.feeFor(2000)).isEqualTo(200);
        assertThat(PaymentService.feeFor(2005)).isEqualTo(201);
        assertThat(PaymentService.feeFor(2004)).isEqualTo(200);
        for (long amount : new long[]{2000, 2005, 2004, 1, 99, 12345}) {
            long fee = PaymentService.feeFor(amount);
            assertThat(fee + (amount - fee)).isEqualTo(amount);
        }
    }

    @Test
    void databaseRefusesASecondPostingWithTheSameSourceReference() {
        Long paystack = accounts.findByName("Paystack receivable").get().getId();
        Long shop = accounts.findByName("Print shop payable").get().getId();
        String source = "payment:dup_" + System.nanoTime() + ":success";
        List<LedgerService.Line> lines = List.of(
            new LedgerService.Line(paystack, Direction.DEBIT, 500),
            new LedgerService.Line(shop, Direction.CREDIT, 500));

        ledger.post("First copy", source, lines);

        assertThatThrownBy(() -> ledger.post("Second copy", source, lines))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
