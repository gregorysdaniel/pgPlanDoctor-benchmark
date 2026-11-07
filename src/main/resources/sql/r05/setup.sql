-- Reset the schema so the run does not depend on previous experiments.
DROP SCHEMA IF EXISTS benchmark_r05 CASCADE;

CREATE SCHEMA benchmark_r05;

CREATE TABLE benchmark_r05.orders (
    id bigint PRIMARY KEY,
    customer_id integer NOT NULL,
    created_at timestamp NOT NULL,
    amount numeric(12, 2) NOT NULL
) WITH (autovacuum_enabled = false);

-- Create one million orders with increasing, deterministic dates.
-- No initial index matches ORDER BY created_at DESC, id DESC.
INSERT INTO benchmark_r05.orders (id, customer_id, created_at, amount)
SELECT n,
       1 + (n % 10000)::integer,
       timestamp '2024-01-01 00:00:00' + n * interval '1 second',
       ((n % 100000) + 1)::numeric / 100
FROM generate_series(1, 1000000) AS series(n);

-- Collect statistics explicitly because autovacuum is disabled.
ANALYZE benchmark_r05.orders;
