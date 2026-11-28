package com.anelda.ledger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    static final long FEE_PERCENT = 10;

    private final PaymentRepository payments;
    private final AccountRepository accounts;
    private final LedgerService ledger;

    public PaymentService(PaymentRepository payments, AccountRepository accounts, LedgerService ledger) {
        this.payments = payments;
        this.accounts = accounts;
        this.ledger = ledger;
    }

    public static long feeFor(long amount) {
        return (amount * FEE_PERCENT + 50) / 100;
    }

    @Transactional
    public void handleSuccess(String reference) {
        Payment payment = payments.findByPaystackReference(reference)
            .orElseThrow(() -> new IllegalArgumentException("Unknown payment " + reference));

        if (payment.getStatus() == PaymentStatus.SUCCESSFUL) {
            return; // already handled: a repeat message does nothing
        }

        payment.moveTo(PaymentStatus.SUCCESSFUL);

        long amount = payment.getAmount();
        long fee = feeFor(amount);
        long shopShare = amount - fee;

        List<LedgerService.Line> lines = new ArrayList<>();
        lines.add(new LedgerService.Line(accountId("Paystack receivable"), Direction.DEBIT, amount));
        lines.add(new LedgerService.Line(accountId("Print shop payable"), Direction.CREDIT, shopShare));
        if (fee > 0) {
            lines.add(new LedgerService.Line(accountId("Platform fee revenue"), Direction.CREDIT, fee));
        }

        ledger.post("Payment " + reference, "payment:" + reference + ":success", lines);
    }

    private Long accountId(String name) {
        return accounts.findByName(name)
            .orElseThrow(() -> new IllegalStateException("Missing account " + name))
            .getId();
    }
}
