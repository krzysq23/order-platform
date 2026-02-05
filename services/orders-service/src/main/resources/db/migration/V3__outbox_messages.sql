CREATE TABLE IF NOT EXISTS outbox_messages (
    id              UUID PRIMARY KEY,
    aggregate_type  VARCHAR(255) NOT NULL,
    aggregate_id    VARCHAR(255) NOT NULL,
    event_type      VARCHAR(255) NOT NULL,
    payload         TEXT NOT NULL,
    occurred_at     TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at    TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_outbox_aggregate
    ON outbox_messages (aggregate_type, aggregate_id);

CREATE INDEX IF NOT EXISTS idx_outbox_processed_at
    ON outbox_messages (processed_at);
