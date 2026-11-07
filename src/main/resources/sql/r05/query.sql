EXPLAIN (ANALYZE, BUFFERS)
SELECT id, customer_id, created_at, amount
FROM benchmark_r05.orders
ORDER BY created_at DESC, id DESC
LIMIT 10;
