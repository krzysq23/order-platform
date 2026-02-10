CREATE TABLE IF NOT EXISTS saga_instances (
  saga_id UUID PRIMARY KEY,
  saga_type VARCHAR(100) NOT NULL,
  aggregate_id VARCHAR(255) NOT NULL,
  state VARCHAR(50) NOT NULL,
  data JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

  CONSTRAINT saga_state_chk CHECK (state IN ('PAYMENT_REQUESTED', 'PAID', 'FAILED', 'CANCELLED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_saga_order
  ON saga_instances(saga_type, aggregate_id);
