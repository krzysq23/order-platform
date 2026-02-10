alter table products
    add column if not exists category_code varchar(32);

insert into product_category(code, name, active)
values ('RTV', 'RTV', true)
on conflict (code) do nothing;

update products
set category_code = 'RTV'
where category_code is null;

alter table products
    alter column category_code set not null;

alter table products
    add constraint fk_products_category
        foreign key (category_code)
        references product_category(code);

create index if not exists ix_products_category_code
    on products(category_code);
