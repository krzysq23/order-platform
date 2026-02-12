insert into product_category(code, name, active)
values
  ('RTV', 'RTV', true),
  ('AGD', 'AGD', true),
  ('SMALL_AGD', 'Małe AGD', true),
  ('AUDIO', 'Audio', true),
  ('TV', 'Telewizory', true),
  ('KITCHEN', 'Kuchnia', true),
  ('CLEANING', 'Sprzątanie', true),
  ('COMPUTING', 'Komputery i peryferia', true)
on conflict (code) do nothing;
