-- Reset the schema so the run does not depend on previous experiments.
DROP SCHEMA IF EXISTS benchmark_r03 CASCADE;

CREATE SCHEMA benchmark_r03;

CREATE TABLE benchmark_r03.orders (
    id bigint PRIMARY KEY,
    customer_id integer NOT NULL,
    status text NOT NULL,
    created_at timestamp NOT NULL,
    amount numeric(12, 2) NOT NULL
) WITH (autovacuum_enabled = false);

-- Customer 1 has 20,000 orders, but only 20 are pending.
-- Other customers are stored in blocks of 98 orders, in ascending customer order.
-- This layout favors an Index Scan by customer, leaving status as a residual filter.
INSERT INTO benchmark_r03.orders (id, customer_id, status, created_at, amount)
SELECT n,
       CASE WHEN n <= 20000 THEN 1 ELSE 2 + ((n - 20001) / 98)::integer END,
       CASE WHEN n <= 20 THEN 'pending' ELSE 'completed' END,
       timestamp '2024-01-01 00:00:00' + n * interval '1 second',
       ((n % 100000) + 1)::numeric / 100
FROM generate_series(1, 1000000) AS series(n);

-- The initial condition uses a single-column index to locate the customer.
CREATE INDEX orders_customer_id_idx ON benchmark_r03.orders (customer_id);

-- Collect statistics explicitly because autovacuum is disabled.
ANALYZE benchmark_r03.orders;
