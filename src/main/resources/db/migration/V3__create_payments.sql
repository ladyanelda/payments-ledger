CREATE TABLE payments (
    id                  BIGSERIAL PRIMARY KEY,
    paystack_reference  TEXT NOT NULL UNIQUE,
    amount              BIGINT NOT NULL CHECK (amount > 0),
    status              TEXT NOT NULL CHECK (status IN ('INITIATED', 'PENDING', 'SUCCESSFUL', 'FAILED', 'REFUNDED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
