package benchmark.experiments;

import benchmark.Config;
import benchmark.Database;
import benchmark.ExperimentUtils;
import benchmark.Results;

import java.io.IOException;
import java.sql.Connection;

public class R01Experiment {
    static final int COUNTRY = 1;
    static final int CURRENCY = 1;

    private final Config config;

    public R01Experiment(Config config) {
        this.config = config;
    }

    public void run() throws Exception {
        final Results results = new Results(config.results(), "r01");
        System.out.println("Results for this run: " + results.directory());

        results.record("experiment", "R01 — row estimate mismatch");
        results.record("hypothesis", "Extended statistics may improve estimates for correlated columns.");
        results.record("dataset", "1,000,000 customers; 100 correlated country and currency pairs");
        results.record("parameters", "country_id = " + COUNTRY + "; currency_id = " + CURRENCY);
        results.record("protocol", ExperimentUtils.protocol());

        final String setup = getResource("setup.sql");
        final String query = getResource("query.sql");
        final String optimization = getResource("optimization.sql");
        results.save("setup.sql", setup);
        results.save("query.sql", query);
        results.save("optimization.sql", optimization);

        try (Connection connection = Database.connect(config)) {
            ExperimentUtils.configureSession(connection);
            ExperimentUtils.recordEnvironment(connection, results);

            System.out.println("Preparing data with standard statistics...");
            Database.executeScript(connection, setup);
            recordStatistics(connection, results, "before");
            ExperimentUtils.runCondition(
                    connection, query, "before", "BEFORE — STANDARD STATISTICS", results, COUNTRY, CURRENCY);

            System.out.println("Creating extended statistics for country_id and currency_id...");
            Database.executeScript(connection, optimization);
            recordStatistics(connection, results, "after");
            ExperimentUtils.runCondition(
                    connection, query, "after", "AFTER — EXTENDED STATISTICS", results, COUNTRY, CURRENCY);
        }

        System.out.println("Experiment complete. Results saved to: " + results.directory());
    }

    private static void recordStatistics(Connection connection, Results results, String condition) throws Exception {
        results.record("statistics_" + condition, Database.queryText(connection, """
                SELECT stxname, stxkind FROM pg_statistic_ext
                WHERE stxnamespace = 'benchmark_r01'::regnamespace ORDER BY stxname
                """));
    }

    private static String getResource(String name) throws IOException {
        final String path = "/sql/r01/" + name;
        return ExperimentUtils.readResource(R01Experiment.class, path);
    }
}
