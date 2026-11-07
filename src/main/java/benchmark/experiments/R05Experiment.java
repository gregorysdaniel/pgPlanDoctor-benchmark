package benchmark.experiments;

import benchmark.Config;
import benchmark.Database;
import benchmark.ExperimentUtils;
import benchmark.Results;

import java.io.IOException;
import java.sql.Connection;

public class R05Experiment {
    private final Config config;

    public R05Experiment(Config config) {
        this.config = config;
    }

    public void run() throws Exception {
        final Results results = new Results(config.results(), "r05");
        System.out.println("Results for this run: " + results.directory());

        results.record("experiment", "R05 — sorting with LIMIT");
        results.record("hypothesis", "An index matching the sort order may avoid sorting the entire table.");
        results.record("dataset", "1,000,000 orders; return the 10 most recent");
        results.record("parameter", "LIMIT 10");
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

            System.out.println("Preparing data without an index matching the sort order...");
            Database.executeScript(connection, setup);
            ExperimentUtils.recordIndexes(connection, results, "benchmark_r05", "orders", "before");
            ExperimentUtils.runCondition(
                    connection, query, "before", "BEFORE — WITHOUT SORT ORDER INDEX", results);

            System.out.println("Creating the index on created_at and id...");
            Database.executeScript(connection, optimization);
            ExperimentUtils.recordIndexes(connection, results, "benchmark_r05", "orders", "after");
            ExperimentUtils.runCondition(
                    connection, query, "after", "AFTER — WITH SORT ORDER INDEX", results);
        }

        System.out.println("Experiment complete. Results saved to: " + results.directory());
    }

    private static String getResource(String name) throws IOException {
        final String path = "/sql/r05/" + name;
        return ExperimentUtils.readResource(R05Experiment.class, path);
    }
}
