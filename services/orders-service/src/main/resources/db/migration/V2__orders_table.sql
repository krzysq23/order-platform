CREATE TABLE IF NOT EXISTS orders (
    id           UUID PRIMARY KEY,
    customer_id  VARCHAR(255) NOT NULL,
    status       VARCHAR(32)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    total_amount DECIMAL(19,2) NOT NULL,
    currency     VARCHAR(3) NOT NULL DEFAULT 'PLN',

    CONSTRAINT orders_status_chk CHECK (status IN ('CREATED', 'CONFIRMED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_created_at  ON orders(created_at);
