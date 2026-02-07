CREATE TABLE IF NOT EXISTS payments (
    id           UUID PRIMARY KEY,
    order_id     UUID          NOT NULL,
    status       VARCHAR(32)   NOT NULL,
    amount       NUMERIC(19,2) NOT NULL,
    currency     VARCHAR(8)    NOT NULL,
    provider     VARCHAR(64)   NULL,
    external_id  VARCHAR(128)  NULL,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT payments_status_chk CHECK (status IN ('REQUESTED', 'AUTHORIZED', 'CAPTURED', 'FAILED', 'CANCELLED')),
    CONSTRAINT payments_amount_chk CHECK (amount > 0),
    CONSTRAINT payments_currency_chk CHECK (currency <> '')
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_payments_order_id ON payments(order_id);

CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at);
CREATE INDEX IF NOT EXISTS idx_payments_status     ON payments(status);
