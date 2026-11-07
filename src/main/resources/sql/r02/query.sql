EXPLAIN (ANALYZE, BUFFERS)
SELECT id, customer_id, status, created_at, amount
FROM benchmark_r02.orders
WHERE customer_id = ?;
