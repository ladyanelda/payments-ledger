package com.anelda.ledger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class LedgerServiceTest {

    @Autowired LedgerService ledger;
    @Autowired BalanceService balances;
    @Autowired AccountRepository accounts;
    @Autowired LedgerTransactionRepository transactions;

    private Account account(String name, AccountType type) {
        return accounts.save(new Account(name + "-" + System.nanoTime(), type));
    }

    @Test
    void balancedPrintPaymentUpdatesAllThreeBalances() {
        Account paystack = account("Paystack receivable", AccountType.ASSET);
        Account shop = account("Print shop payable", AccountType.LIABILITY);
        Account fee = account("Platform fee revenue", AccountType.REVENUE);

        ledger.post("Student print payment", List.of(
            new LedgerService.Line(paystack.getId(), Direction.DEBIT, 2000),
            new LedgerService.Line(shop.getId(), Direction.CREDIT, 1800),
            new LedgerService.Line(fee.getId(), Direction.CREDIT, 200)
        ));

        assertThat(balances.balanceOf(paystack.getId())).isEqualTo(2000);
        assertThat(balances.balanceOf(shop.getId())).isEqualTo(1800);
        assertThat(balances.balanceOf(fee.getId())).isEqualTo(200);
    }

    @Test
    void unbalancedTransactionIsRejectedAndNothingIsSaved() {
        Account paystack = account("Paystack receivable", AccountType.ASSET);
        Account shop = account("Print shop payable", AccountType.LIABILITY);
        long before = transactions.count();

        assertThatThrownBy(() -> ledger.post("Missing fee line", List.of(
            new LedgerService.Line(paystack.getId(), Direction.DEBIT, 2000),
            new LedgerService.Line(shop.getId(), Direction.CREDIT, 1800)
        ))).isInstanceOf(IllegalArgumentException.class);

        assertThat(transactions.count()).isEqualTo(before);
        assertThat(balances.balanceOf(paystack.getId())).isEqualTo(0);
    }

    @Test
    void refundBringsEveryBalanceBackToZero() {
        Account paystack = account("Paystack receivable", AccountType.ASSET);
        Account shop = account("Print shop payable", AccountType.LIABILITY);
        Account fee = account("Platform fee revenue", AccountType.REVENUE);

        ledger.post("Student print payment", List.of(
            new LedgerService.Line(paystack.getId(), Direction.DEBIT, 2000),
            new LedgerService.Line(shop.getId(), Direction.CREDIT, 1800),
            new LedgerService.Line(fee.getId(), Direction.CREDIT, 200)
        ));
        ledger.post("Refund print payment", List.of(
            new LedgerService.Line(paystack.getId(), Direction.CREDIT, 2000),
            new LedgerService.Line(shop.getId(), Direction.DEBIT, 1800),
            new LedgerService.Line(fee.getId(), Direction.DEBIT, 200)
        ));

        assertThat(balances.balanceOf(paystack.getId())).isZero();
        assertThat(balances.balanceOf(shop.getId())).isZero();
        assertThat(balances.balanceOf(fee.getId())).isZero();
    }
}
