package benchmark.experiments;

import benchmark.Config;
import benchmark.Database;
import benchmark.ExperimentUtils;
import benchmark.Results;

import java.io.IOException;
import java.sql.Connection;

public class R02Experiment {
    static final int CUSTOMER = 1;

    private final Config config;

    public R02Experiment(Config config) {
        this.config = config;
    }

    public void run() throws Exception {
        final Results results = new Results(config.results(), "r02");

        System.out.println("Results for this run: " + results.directory());

        results.record("experiment", "R02 — selective filter");
        results.record("hypothesis", "An index on customer_id may reduce read work for the rare customer.");
        results.record("dataset", "1,000,000 orders; customer 1: 100; customer 2: 500,000");
        results.record("parameter", "customer_id = " + CUSTOMER);
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

            System.out.println("Preparing data without the candidate index...");
            Database.executeScript(connection, setup);
            ExperimentUtils.recordIndexes(connection, results, "benchmark_r02", "orders", "before");
            ExperimentUtils.runCondition(
                    connection, query, "before", "BEFORE — WITHOUT CANDIDATE INDEX", results, CUSTOMER);

            System.out.println("Creating the index on customer_id...");
            Database.executeScript(connection, optimization);
            ExperimentUtils.recordIndexes(connection, results, "benchmark_r02", "orders", "after");
            ExperimentUtils.runCondition(
                    connection, query, "after", "AFTER — WITH CANDIDATE INDEX", results, CUSTOMER);
        }

        System.out.println("Experiment complete. Results saved to: " + results.directory());
    }

    private static String getResource(String name) throws IOException {
        final String path = "/sql/r02/" + name;
        return ExperimentUtils.readResource(R02Experiment.class, path);
    }
}
