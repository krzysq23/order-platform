UPDATE orders
SET status = 'PAID'
WHERE status = 'CONFIRMED';

ALTER TABLE orders
  DROP CONSTRAINT IF EXISTS orders_status_chk;

ALTER TABLE orders
  ADD CONSTRAINT orders_status_chk
  CHECK (status IN (
    'CREATED',
    'PAYMENT_PENDING',
    'PAID',
    'PAYMENT_FAILED',
    'CANCELLED'
  ));
