CREATE TABLE accounts (
    id          BIGSERIAL PRIMARY KEY,
    name        TEXT NOT NULL UNIQUE,
    type        TEXT NOT NULL CHECK (type IN ('ASSET', 'LIABILITY', 'REVENUE'))
);

CREATE TABLE transactions (
    id          BIGSERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE entries (
    id              BIGSERIAL PRIMARY KEY,
    transaction_id  BIGINT NOT NULL REFERENCES transactions(id),
    account_id      BIGINT NOT NULL REFERENCES accounts(id),
    direction       TEXT NOT NULL CHECK (direction IN ('DEBIT', 'CREDIT')),
    amount          BIGINT NOT NULL CHECK (amount > 0)
);
