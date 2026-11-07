# Selected heuristics

Rules for investigating estimation, access, filtering, sorting, and repeated work in PostgreSQL joins. Each rule connects a sign in the plan to a recommendation, with limitations and references. These signs guide investigation without guaranteeing a performance improvement.

<a id="r01"></a>

## R01 — Row estimate mismatch

**Sign:** a large difference between estimated and actual rows, compared per execution.

**Investigate:** refreshing statistics and adding extended statistics for correlated columns. For example, a node returns 100 times more rows than estimated.

**Limitations:** better estimates may not change the plan or execution time. Partially consumed nodes, such as some children of `LIMIT`, require separate consideration.

[Planner statistics](https://www.postgresql.org/docs/17/planner-stats.html)

<a id="r02"></a>

## R02 — Broad scan for a selective filter

**Sign:** a `Seq Scan` with a high proportion of discarded rows and a substantial absolute row count. For example, scanning one million candidates to return 100 rows.

**Investigate:** an index matching the filter; compare the work needed to access rows, execution time, and buffers.

**Limitations:** small tables may not benefit. A sequential scan does not prove that an index is missing; check existing indexes. Indexes add storage and write costs.

[Indexes](https://www.postgresql.org/docs/17/indexes-intro.html)

<a id="r03"></a>

## R03 — Index access with many rows removed by a residual filter

**Sign:** an `Index Scan` with one condition in `Index Cond` and another in `Filter`, discarding many candidates.

**Investigate:** a composite index matching both conditions, such as customer and status.

**Limitations:** column order depends on the predicates and existing indexes. A residual filter alone is not enough to justify a change. Composite indexes add storage and maintenance costs.

[Multicolumn indexes](https://www.postgresql.org/docs/17/indexes-multicolumn.html)

<a id="r04"></a>

## R04 — Expression in a filter with a broad scan

**Sign:** an expression such as `lower(email)` in a filter, with a broad scan and many discarded rows.

**Investigate:** an index matching the expression. A regular email index is not equivalent to an index on `lower(email)`.

**Limitations:** a function alone does not indicate a problem. Removing it may change query results. Expression indexes add storage, expression evaluation, and maintenance costs.

[Indexes on expressions](https://www.postgresql.org/docs/17/indexes-expressional.html)

<a id="r05"></a>

## R05 — Sorting many rows to return a few

**Sign:** a `Limit` above a `Sort`, with an input much larger than the requested limit.

**Investigate:** an index matching the sort order and filters, allowing the scan to stop earlier.

**Limitations:** the benefit depends on the filters and requested row count. Do not add a `LIMIT` that changes the required result. The index also adds storage and write costs.

[Indexes and ORDER BY](https://www.postgresql.org/docs/17/indexes-ordering.html)

<a id="r06"></a>

## R06 — Costly repeated access on the inner side of a join

**Sign:** many `loops` on the inner side of a `Nested Loop`, with substantial cumulative work.

**Investigate:** an index matching the join condition to reduce the cost of each lookup.

**Limitations:** many fast lookups can be efficient. `Memoize` and `Materialize` can reuse results. Check the work actually repeated; do not recommend another algorithm based only on the loop count. New indexes also have maintenance costs.

[Joins and execution metrics](https://www.postgresql.org/docs/17/using-explain.html)
