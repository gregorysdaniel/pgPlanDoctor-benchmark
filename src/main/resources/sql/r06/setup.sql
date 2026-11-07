-- Reset the schema so the run does not depend on previous experiments.
DROP SCHEMA IF EXISTS benchmark_r06 CASCADE;

CREATE SCHEMA benchmark_r06;

CREATE TABLE benchmark_r06.customers (
    id integer PRIMARY KEY,
    name text NOT NULL
) WITH (autovacuum_enabled = false);

CREATE TABLE benchmark_r06.orders (
    id bigint PRIMARY KEY,
    customer_id integer NOT NULL REFERENCES benchmark_r06.customers(id),
    amount numeric(12, 2) NOT NULL
) WITH (autovacuum_enabled = false);

-- Create 1,000 customers and 100,000 orders evenly distributed among them.
-- The query sums order amounts for the first 100 customers.
INSERT INTO benchmark_r06.customers (id, name)
SELECT n, 'Customer ' || n
FROM generate_series(1, 1000) AS series(n);

INSERT INTO benchmark_r06.orders (id, customer_id, amount)
SELECT n,
       1 + ((n - 1) % 1000)::integer,
       ((n % 100000) + 1)::numeric / 100
FROM generate_series(1, 100000) AS series(n);

-- There is no initial index on customer_id, so each inner lookup scans all orders.
ANALYZE benchmark_r06.customers;
ANALYZE benchmark_r06.orders;
