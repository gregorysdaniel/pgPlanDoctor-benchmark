CREATE STATISTICS benchmark_r01.customers_country_currency_stats (dependencies)
ON country_id, currency_id
FROM benchmark_r01.customers;

ANALYZE benchmark_r01.customers;
