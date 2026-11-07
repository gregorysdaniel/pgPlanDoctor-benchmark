EXPLAIN (ANALYZE, BUFFERS)
SELECT customer.id, customer.name, totals.total_amount
FROM benchmark_r06.customers AS customer
JOIN LATERAL (
    SELECT sum(orders.amount) AS total_amount
    FROM benchmark_r06.orders
    WHERE orders.customer_id = customer.id
) AS totals ON true
WHERE customer.id <= ?;
