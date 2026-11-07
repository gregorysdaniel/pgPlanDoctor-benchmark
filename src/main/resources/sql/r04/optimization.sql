CREATE INDEX customers_lower_email_idx ON benchmark_r04.customers (lower(email));

ANALYZE benchmark_r04.customers;
