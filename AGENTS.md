# Project guidelines

This repository contains a Java 17 and PostgreSQL 17 benchmark for comparing execution plans before and after changes to indexes or statistics.

## Code and experiments

- Keep queries and SQL scripts in `src/main/resources/sql/r01` through `r06`.
- Preserve deterministic data, schema resets, and identical parameters across conditions.
- Keep one warmup and five measurements per condition. Reflect protocol changes in the README and the context saved with each run.
- Preserve complete `EXPLAIN (ANALYZE, BUFFERS)` plans and existing results.
- Do not present an experiment's hypothesis as a measured result.

## Documentation and comments

- Write documentation, comments, and program messages in English, using the project's technical terms and API names.
- Describe current behavior, execution instructions, and experimental limitations.
- Use comments to explain decisions or experimental conditions that are not clear from the code.
- Avoid co
- 
- mments that merely repeat a method name or describe an obvious statement.
- Keep technical references with their corresponding heuristics.

## Verification

- Build with `mvn package` using JDK 17.
- When changing an experiment, run it against the Compose database and check the scripts, context, and six plans per condition in the output directory.
- Rebuild after changing SQL scripts, since they are bundled in the JAR.
