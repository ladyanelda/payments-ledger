package com.anelda.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    @Query("select coalesce(sum(e.amount), 0L) from Entry e " +
           "where e.account.id = :accountId and e.direction = :direction")
    long sumFor(@Param("accountId") Long accountId, @Param("direction") Direction direction);
}
