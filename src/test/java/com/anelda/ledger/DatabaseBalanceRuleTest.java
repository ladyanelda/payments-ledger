package com.anelda.ledger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class DatabaseBalanceRuleTest {

    @Autowired JdbcTemplate jdbc;
    @Autowired AccountRepository accounts;

    @Test
    void databaseRejectsUnbalancedEntriesWrittenDirectly() {
        Account paystack = accounts.save(
            new Account("Paystack receivable-" + System.nanoTime(), AccountType.ASSET));

        Long txId = jdbc.queryForObject(
            "insert into transactions (description) values ('raw SQL, skipping Java') returning id",
            Long.class);

        assertThatThrownBy(() -> jdbc.update(
            "insert into entries (transaction_id, account_id, direction, amount) values (?, ?, 'DEBIT', 2000)",
            txId, paystack.getId()))
            .isInstanceOf(DataAccessException.class)
            .hasStackTraceContaining("unbalanced");

        Integer saved = jdbc.queryForObject(
            "select count(*) from entries where transaction_id = ?", Integer.class, txId);
        assertThat(saved).isZero();
    }
}
