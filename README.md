# PostgreSQL Benchmark

Java 17 benchmark for studying six PostgreSQL query optimization heuristics. Each experiment runs the same query before and after a change to indexes or statistics and saves the complete `EXPLAIN (ANALYZE, BUFFERS)` plans.

The project collects scripts, execution conditions, and results for experimental analysis in an undergraduate thesis. The [selected heuristics](core_heuristics.md) describe the signs to look for in the plans and the recommendations being evaluated.

## Running the benchmark

Requirements: JDK 17, Maven 3.8+, and Docker Compose. From the project root:

```bash
docker compose up -d --wait
mvn clean package
java -jar target/benchmark.jar
```

Choose **1 through 6** from the menu to run an experiment, or **0** to exit. The program prepares the data. If several JDKs are installed, set `JAVA_HOME` to JDK 17.

To stop the database, run `docker compose stop`.

## Experiments

| Option | Heuristic | Dataset | Initial condition | Change |
|---|---|---|---|---|
| 1 | [R01 — cardinality estimates](core_heuristics.md#r01) | One million customers; 100 correlated country and currency pairs | Standard statistics | Add dependency statistics and run `ANALYZE` |
| 2 | [R02 — selective filter](core_heuristics.md#r02) | One million orders; 100 for the target customer | No customer index | Create an index on `customer_id` |
| 3 | [R03 — residual filtering](core_heuristics.md#r03) | One million orders; 20,000 for the target customer, of which 20 are pending | Single-column customer index | Add a composite index, keeping the single-column index |
| 4 | [R04 — function in a filter](core_heuristics.md#r04) | One million customers with unique emails | Regular email index | Add an index on `lower(email)`, keep the regular index, and run `ANALYZE` |
| 5 | [R05 — sorting with LIMIT](core_heuristics.md#r05) | One million orders with dates and IDs; return the first 10 | No index matching the sort order | Add an index matching the sort order |
| 6 | [R06 — repeated lookups in a join](core_heuristics.md#r06) | 1,000 customers and 100,000 orders; totals for the first 100 customers | No index on `orders.customer_id` | Add an index on `orders.customer_id` |

The data is synthetic and deterministic, with one fixed dataset per heuristic. Each distribution was chosen to expose the issue being studied and favor the proposed change.

R03 and R04 keep the original indexes. R05 uses `ORDER BY created_at DESC, id DESC` and `LIMIT 10` in both conditions. R06 calculates a total per customer with `JOIN LATERAL`, without disabling PostgreSQL join methods.

## Execution protocol

Each run follows these steps:

1. Record the hypothesis, parameters, scripts, and database settings.
2. Recreate the experiment's schema with the data, indexes, and statistics for the initial condition.
3. Run the query once for warmup and five times for measurement.
4. Apply the change and record the resulting indexes or statistics.
5. Repeat the query with the same parameters, again with one warmup and five measurements.

The data stays the same across both conditions. Setup, index creation, and statistics collection are excluded from the measurements. Each experiment recreates only its own schema (`benchmark_r01` through `benchmark_r06`).

Parallelism and JIT are disabled for the session. Experimental tables have autovacuum/autoanalyze disabled, and `ANALYZE` runs explicitly. The JDBC connection uses `prepareThreshold=0`. The PostgreSQL version and the parallelism and JIT settings are recorded with the results.

The cache is not cleared between conditions. Data preparation and index creation can also warm it.

## Results

Each run creates a directory at `results/<experiment>-<UTC timestamp>/` containing:

- `context.txt`: hypothesis, dataset description, parameters, database version, settings, and indexes or statistics for each condition.
- `setup.sql`, `query.sql`, and `optimization.sql`: copies of the scripts used.
- `before.txt` and `after.txt`: complete plans for the initial condition and the condition after the change.

Each plan file contains six executions, labeled `WARMUP 1` and `MEASUREMENT 1` through `MEASUREMENT 5`. Plans are printed to the terminal and saved in UTF-8. Repeating an experiment creates another directory, preserving previous results.

## Analysis and limitations

Plan analysis is manual. Compare estimated and actual rows, filters, access methods, execution times, and buffers across conditions. The five measurements show timing variation within each condition.

Account for the number of `loops` when assessing repeated work. Parent node times include work done by their children and should not be added indiscriminately. In R06, also check for reuse through `Memoize` or `Materialize`.

Each dataset represents a case favorable to the heuristic. A positive result demonstrates its behavior under these conditions, without guaranteeing a benefit for other queries or distributions. Repeated runs do not replace dataset diversity. The benchmark does not measure index maintenance costs during writes.

The program records plans even when the change does not improve performance. It does not calculate speedups or automatically rank heuristics.

Technical references are available in the [heuristic catalog](core_heuristics.md) and the PostgreSQL documentation on [EXPLAIN](https://www.postgresql.org/docs/17/sql-explain.html) and [reading plans](https://www.postgresql.org/docs/17/using-explain.html).

## Connection settings

Compose starts PostgreSQL 17 at `localhost:55432`, with database `benchmark_experiments`, user `benchmark`, and password `benchmark_local`. The port is exposed only on the loopback interface.

The Java program accepts the following environment variables:

| Variable | Default |
|---|---|
| `BENCHMARK_DB_HOST` | `localhost` |
| `BENCHMARK_DB_PORT` | `55432` |
| `BENCHMARK_DB_USER` | `benchmark` |
| `BENCHMARK_DB_PASSWORD` | `benchmark_local` |
| `BENCHMARK_RESULTS_DIR` | `results` |

The database name is fixed. To customize the Compose port or password, copy `.env.example` to `.env` and edit the values. Export the same variables before starting Java:

```bash
set -a
. ./.env
set +a
```

Compose reads `.env` automatically. Changing the password in that file does not update an existing database volume.

## Project structure

- `src/main/java/benchmark/`: menu, JDBC connection, and result handling.
- `src/main/java/benchmark/experiments/`: experiment classes R01–R06.
- `src/main/resources/sql/`: setup, query, and optimization scripts for each experiment. Rebuild after editing.
- [core_heuristics.md](core_heuristics.md): signs, recommendations, limitations, and references for each heuristic.
