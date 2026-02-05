ALTER TABLE outbox_messages
  ADD COLUMN IF NOT EXISTS attempts          integer      NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS next_attempt_at   timestamptz  NULL,
  ADD COLUMN IF NOT EXISTS locked_at         timestamptz  NULL,
  ADD COLUMN IF NOT EXISTS locked_by         varchar(128) NULL,
  ADD COLUMN IF NOT EXISTS last_error        text         NULL;

CREATE INDEX IF NOT EXISTS idx_outbox_unprocessed
  ON outbox_messages (processed_at)
  WHERE processed_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_outbox_next_attempt
  ON outbox_messages (next_attempt_at)
  WHERE processed_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_outbox_locked_at
  ON outbox_messages (locked_at)
  WHERE processed_at IS NULL;
