ALTER TABLE outbox_messages
ADD COLUMN event_id uuid;

UPDATE outbox_messages
SET event_id = gen_random_uuid()
WHERE event_id IS NULL;

ALTER TABLE outbox_messages
ALTER COLUMN event_id SET NOT NULL;

ALTER TABLE outbox_messages
ADD CONSTRAINT ux_outbox_event_id UNIQUE (event_id);
