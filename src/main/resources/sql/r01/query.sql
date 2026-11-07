EXPLAIN (ANALYZE, BUFFERS)
SELECT id, country_id, currency_id, name
FROM benchmark_r01.customers
WHERE country_id = ? AND currency_id = ?;
