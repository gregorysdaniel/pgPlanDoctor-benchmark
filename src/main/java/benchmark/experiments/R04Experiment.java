package benchmark.experiments;

import benchmark.Config;
import benchmark.Database;
import benchmark.ExperimentUtils;
import benchmark.Results;

import java.io.IOException;
import java.sql.Connection;

public class R04Experiment {
    static final String EMAIL = "user1@example.com";

    private final Config config;

    public R04Experiment(Config config) {
        this.config = config;
    }

    public void run() throws Exception {
        final Results results = new Results(config.results(), "r04");
        System.out.println("Results for this run: " + results.directory());

        results.record("experiment", "R04 — function in a filter");
        results.record("hypothesis", "An index on lower(email) may support a filter using the same expression.");
        results.record("dataset", "1,000,000 customers with unique emails");
        results.record("parameter", "lower(email) = " + EMAIL);
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

            System.out.println("Preparing data with a regular index on email...");
            Database.executeScript(connection, setup);
            ExperimentUtils.recordIndexes(connection, results, "benchmark_r04", "customers", "before");
            ExperimentUtils.runCondition(
                    connection, query, "before", "BEFORE — REGULAR INDEX", results, EMAIL);

            System.out.println("Creating the index on lower(email) and collecting its statistics...");
            Database.executeScript(connection, optimization);
            ExperimentUtils.recordIndexes(connection, results, "benchmark_r04", "customers", "after");
            ExperimentUtils.runCondition(
                    connection, query, "after", "AFTER — EXPRESSION INDEX", results, EMAIL);
        }

        System.out.println("Experiment complete. Results saved to: " + results.directory());
    }

    private static String getResource(String name) throws IOException {
        final String path = "/sql/r04/" + name;
        return ExperimentUtils.readResource(R04Experiment.class, path);
    }
}
