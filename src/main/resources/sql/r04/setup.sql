-- Reset the schema so the run does not depend on previous experiments.
DROP SCHEMA IF EXISTS benchmark_r04 CASCADE;

CREATE SCHEMA benchmark_r04;

CREATE TABLE benchmark_r04.customers (
    id bigint PRIMARY KEY,
    name text NOT NULL,
    email text NOT NULL
) WITH (autovacuum_enabled = false);

-- Create one million customers with unique, deterministic names and emails.
INSERT INTO benchmark_r04.customers (id, name, email)
SELECT n,
       'Customer ' || n,
       'user' || n || '@example.com'
FROM generate_series(1, 1000000) AS series(n);

-- The regular email index does not directly support the lower(email) filter.
CREATE INDEX customers_email_idx ON benchmark_r04.customers (email);

-- Collect statistics explicitly because autovacuum is disabled.
ANALYZE benchmark_r04.customers;
