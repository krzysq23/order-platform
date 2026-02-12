create table if not exists product_category (
    code        varchar(32) primary key,
    name        varchar(128) not null,
    active      boolean not null default true,
    created_at  timestamptz not null default now()
);

create index if not exists ix_product_category_active
    on product_category(active);
