EXPLAIN (ANALYZE, BUFFERS)
SELECT id, name, email
FROM benchmark_r04.customers
WHERE lower(email) = ?;
