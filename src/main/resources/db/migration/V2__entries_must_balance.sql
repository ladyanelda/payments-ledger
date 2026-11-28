CREATE FUNCTION check_transaction_balanced() RETURNS trigger AS $$
DECLARE
    tx_id BIGINT;
    total BIGINT;
BEGIN
    tx_id := COALESCE(NEW.transaction_id, OLD.transaction_id);

    SELECT COALESCE(SUM(CASE WHEN direction = 'DEBIT' THEN amount ELSE -amount END), 0)
      INTO total
      FROM entries
     WHERE transaction_id = tx_id;

    IF total <> 0 THEN
        RAISE EXCEPTION 'Transaction % is unbalanced (debits minus credits = %)', tx_id, total;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER entries_must_balance
    AFTER INSERT OR UPDATE OR DELETE ON entries
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
    EXECUTE FUNCTION check_transaction_balanced();
