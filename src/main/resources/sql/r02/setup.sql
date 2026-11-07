-- Reset the schema so the run does not depend on previous experiments.
DROP SCHEMA IF EXISTS benchmark_r02 CASCADE;

CREATE SCHEMA benchmark_r02;

CREATE TABLE benchmark_r02.orders (
    id bigint PRIMARY KEY,
    customer_id integer NOT NULL,
    status text NOT NULL,
    created_at timestamp NOT NULL,
    amount numeric(12, 2) NOT NULL
) WITH (autovacuum_enabled = false);

-- Create one million orders with a deterministic distribution:
-- customer 1 has only 100 orders, customer 2 has 500,000,
-- and the remaining orders are distributed among other customers.
INSERT INTO benchmark_r02.orders (id, customer_id, status, created_at, amount)
SELECT n,
       CASE WHEN n <= 100 THEN 1
            WHEN n <= 500100 THEN 2
            ELSE 3 + (n % 10000)::integer END,
       CASE WHEN n % 4 = 0 THEN 'pending' ELSE 'completed' END,
       timestamp '2024-01-01 00:00:00' + n * interval '1 second',
       ((n % 100000) + 1)::numeric / 100
FROM generate_series(1, 1000000) AS series(n);

-- Collect statistics explicitly because autovacuum is disabled.
ANALYZE benchmark_r02.orders;
