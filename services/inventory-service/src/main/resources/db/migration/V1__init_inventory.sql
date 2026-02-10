-- V1__init_inventory.sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =========================
-- 1) PRODUCTS
-- =========================
CREATE TABLE IF NOT EXISTS products (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku             VARCHAR(64) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_products_active ON products (is_active);

-- =========================
-- 2) STOCK
-- =========================
CREATE TABLE IF NOT EXISTS stock_items (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id          UUID NOT NULL REFERENCES products(id),
    warehouse_code      VARCHAR(32) NOT NULL,
    quantity_on_hand    INTEGER NOT NULL CHECK (quantity_on_hand >= 0),
    quantity_reserved   INTEGER NOT NULL CHECK (quantity_reserved >= 0),
    version             BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_stock_items_product_warehouse UNIQUE (product_id, warehouse_code),
    CONSTRAINT chk_stock_reserved_not_exceed CHECK (quantity_reserved <= quantity_on_hand)
);

CREATE INDEX IF NOT EXISTS idx_stock_items_product
    ON stock_items (product_id);

CREATE INDEX IF NOT EXISTS idx_stock_items_warehouse
    ON stock_items (warehouse_code);

CREATE INDEX IF NOT EXISTS idx_stock_items_available
    ON stock_items (product_id, warehouse_code, quantity_on_hand, quantity_reserved);

-- =========================
-- 3) RESERVATIONS
-- =========================
CREATE TABLE IF NOT EXISTS stock_reservations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID NOT NULL UNIQUE,
    status              VARCHAR(32) NOT NULL,
    correlation_id      UUID NULL,
    expires_at          TIMESTAMPTZ NULL,
    reason              VARCHAR(512) NULL,
    data                JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_stock_reservation_status
        CHECK (status IN ('REQUESTED','RESERVED','FAILED','CANCELLED','RELEASED','EXPIRED'))
);

CREATE INDEX IF NOT EXISTS idx_stock_reservations_status ON stock_reservations (status);
CREATE INDEX IF NOT EXISTS idx_stock_reservations_expires_at ON stock_reservations (expires_at);

-- =========================
-- 4) RESERVATION LINES
-- =========================
CREATE TABLE IF NOT EXISTS stock_reservation_lines (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id      UUID NOT NULL REFERENCES stock_reservations(id) ON DELETE CASCADE,
    product_id          UUID NOT NULL REFERENCES products(id),
    warehouse_code      VARCHAR(32) NOT NULL,
    quantity            INTEGER NOT NULL CHECK (quantity > 0),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_stock_res_lines_reservation
    ON stock_reservation_lines (reservation_id);

CREATE INDEX IF NOT EXISTS idx_stock_res_lines_product
    ON stock_reservation_lines (product_id);

CREATE INDEX IF NOT EXISTS idx_stock_res_lines_product_wh
    ON stock_reservation_lines (product_id, warehouse_code);

CREATE UNIQUE INDEX IF NOT EXISTS uq_stock_res_lines_unique_item
    ON stock_reservation_lines (reservation_id, product_id, warehouse_code);

-- =========================
-- 5) IDEMPOTENCY (processed events)
-- =========================
CREATE TABLE IF NOT EXISTS processed_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id         UUID NOT NULL UNIQUE,
    event_type       VARCHAR(128) NOT NULL,
    occurred_at      TIMESTAMPTZ NULL,
    aggregate_id     UUID NULL,
    payload_hash     VARCHAR(128) NULL,
    processed_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_processed_events_type_time
    ON processed_events (event_type, processed_at DESC);

-- =========================
-- 6) OUTBOX (publisher)
-- =========================
CREATE TABLE IF NOT EXISTS outbox_messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id         UUID NOT NULL UNIQUE,
    event_type       VARCHAR(128) NOT NULL,
    version          INTEGER NOT NULL DEFAULT 1,
    occurred_at      TIMESTAMPTZ NOT NULL,
    aggregate_type   VARCHAR(64) NOT NULL,
    aggregate_id     UUID NOT NULL,
    topic            VARCHAR(255) NULL,
    key             VARCHAR(255) NULL,
    payload          JSONB NOT NULL,

    attempts         INTEGER NOT NULL DEFAULT 0,
    next_attempt_at  TIMESTAMPTZ NULL,
    locked_at        TIMESTAMPTZ NULL,
    locked_by        VARCHAR(128) NULL,
    processed_at     TIMESTAMPTZ NULL,
    last_error       TEXT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_outbox_unprocessed_next_attempt
    ON outbox_messages (processed_at, next_attempt_at)
    WHERE processed_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_outbox_locking
    ON outbox_messages (locked_at, locked_by)
    WHERE processed_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_outbox_event_type
    ON outbox_messages (event_type, version)
    WHERE processed_at IS NULL;
