alter table saga_instances
    drop constraint if exists saga_state_chk;

alter table saga_instances
    add constraint saga_state_chk check (
        state in (
            'INVENTORY_REQUESTED',
            'INVENTORY_RESERVED',
            'PAYMENT_REQUESTED',
            'PAID',
            'PAYMENT_FAILED',
            'CANCELLED'
        )
    );


alter table orders
    drop constraint if exists orders_status_chk;

alter table orders
    add constraint orders_status_chk
        check (status in (
            'CREATED',
            'INVENTORY_PENDING',
            'PAYMENT_PENDING',
            'PAID',
            'PAYMENT_FAILED',
            'CANCELLED'
        ));
