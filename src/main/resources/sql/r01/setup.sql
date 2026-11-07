-- Reset the schema so the run does not depend on previous experiments.
DROP SCHEMA IF EXISTS benchmark_r01 CASCADE;

CREATE SCHEMA benchmark_r01;

CREATE TABLE benchmark_r01.customers (
    id bigint PRIMARY KEY,
    country_id integer NOT NULL,
    currency_id integer NOT NULL,
    name text NOT NULL
) WITH (autovacuum_enabled = false);

-- Create one million customers evenly distributed across 100 countries.
-- Each country uses a single currency, correlating country_id and currency_id.
-- The query for the pair 1 and 1 returns 10,000 rows.
INSERT INTO benchmark_r01.customers (id, country_id, currency_id, name)
SELECT n,
       1 + ((n - 1) % 100)::integer,
       1 + ((n - 1) % 100)::integer,
       'Customer ' || n
FROM generate_series(1, 1000000) AS series(n);

-- The initial condition has only standard statistics for each individual column.
ANALYZE benchmark_r01.customers;
