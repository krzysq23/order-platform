create extension if not exists pgcrypto;

insert into product_category(code, name, active)
values
  ('RTV', 'RTV', true),
  ('AGD', 'AGD', true)
on conflict (code) do nothing;

with products_data (sku, name, category_code) as (
    select * from (values
        ('TV-SAMSUNG-55-QLED', 'Samsung QLED TV 55"', 'RTV'),
        ('TV-LG-65-OLED', 'LG OLED TV 65"', 'RTV'),
        ('TV-SONY-50-LED', 'Sony LED TV 50"', 'RTV'),
        ('TV-PHILIPS-43-LED', 'Philips LED TV 43"', 'RTV'),
        ('TV-TCL-55-QLED', 'TCL QLED TV 55"', 'RTV'),

        ('WASHER-BOSCH-9KG', 'Bosch Washing Machine 9kg', 'AGD'),
        ('WASHER-SAMSUNG-8KG', 'Samsung Washing Machine 8kg', 'AGD'),
        ('WASHER-LG-10KG', 'LG Washing Machine 10kg', 'AGD'),

        ('FRIDGE-LG-350L', 'LG Refrigerator 350L', 'AGD'),
        ('FRIDGE-SAMSUNG-400L', 'Samsung Refrigerator 400L', 'AGD'),
        ('FRIDGE-BOSCH-320L', 'Bosch Refrigerator 320L', 'AGD'),

        ('DISHWASHER-BOSCH', 'Bosch Dishwasher', 'AGD'),
        ('DISHWASHER-SIEMENS', 'Siemens Dishwasher', 'AGD'),

        ('MICROWAVE-SAMSUNG', 'Samsung Microwave', 'AGD'),
        ('MICROWAVE-LG', 'LG Microwave', 'AGD'),

        ('VACUUM-DYSON-V11', 'Dyson V11 Vacuum Cleaner', 'AGD'),
        ('VACUUM-PHILIPS', 'Philips Vacuum Cleaner', 'AGD'),

        ('COFFEE-DE-LONGHI', 'DeLonghi Coffee Machine', 'AGD'),
        ('COFFEE-KRUPS', 'Krups Coffee Machine', 'AGD'),

        ('SOUNDBAR-SONY', 'Sony Soundbar', 'RTV'),
        ('SOUNDBAR-SAMSUNG', 'Samsung Soundbar', 'RTV'),

        ('HEADPHONES-SONY-WH1000XM5', 'Sony WH-1000XM5 Headphones', 'RTV'),
        ('HEADPHONES-BOSE-QC45', 'Bose QuietComfort 45', 'RTV'),

        ('SMARTWATCH-APPLE-SE', 'Apple Watch SE', 'RTV'),
        ('SMARTWATCH-SAMSUNG-GALAXY', 'Samsung Galaxy Watch', 'RTV')
    ) base
)
insert into products (id, sku, name, category_code, is_active, created_at)
select
    gen_random_uuid(),
    sku,
    name,
    category_code,
    true,
    now()
from products_data
on conflict (sku) do nothing;

insert into products (id, sku, name, category_code, is_active, created_at)
select
    gen_random_uuid(),
    'RTV-GENERIC-' || gs,
    'Generic RTV Product #' || gs,
    'RTV',
    true,
    now()
from generate_series(1, 80) gs
on conflict (sku) do nothing;

insert into stock_items (
    id,
    product_id,
    warehouse_code,
    quantity_on_hand,
    quantity_reserved,
    created_at
)
select
    gen_random_uuid(),
    p.id,
    'MAIN',
    50 + (random() * 100)::int,
    0,
    now()
from products p
left join stock_items si
    on si.product_id = p.id
   and si.warehouse_code = 'MAIN'
where si.id is null;
