package benchmark.experiments;

import benchmark.Config;
import benchmark.Database;
import benchmark.ExperimentUtils;
import benchmark.Results;

import java.io.IOException;
import java.sql.Connection;

public class R03Experiment {
    static final int CUSTOMER = 1;
    static final String STATUS = "pending";

    private final Config config;

    public R03Experiment(Config config) {
        this.config = config;
    }

    public void run() throws Exception {
        final Results results = new Results(config.results(), "r03");
        System.out.println("Results for this run: " + results.directory());

        results.record("experiment", "R03 — single-column versus composite index");
        results.record("hypothesis", "A composite index may avoid residual filtering by status.");
        results.record("dataset", "1,000,000 orders; customer 1: 20,000; pending status: 20");
        results.record("parameters", "customer_id = " + CUSTOMER + "; status = " + STATUS);
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

            System.out.println("Preparing data with a single-column index on customer_id...");
            Database.executeScript(connection, setup);
            ExperimentUtils.recordIndexes(connection, results, "benchmark_r03", "orders", "before");
            ExperimentUtils.runCondition(
                    connection, query, "before", "BEFORE — SINGLE-COLUMN INDEX", results, CUSTOMER, STATUS);

            System.out.println("Creating the composite index on customer_id and status...");
            Database.executeScript(connection, optimization);
            ExperimentUtils.recordIndexes(connection, results, "benchmark_r03", "orders", "after");
            ExperimentUtils.runCondition(
                    connection, query, "after", "AFTER — COMPOSITE INDEX", results, CUSTOMER, STATUS);
        }

        System.out.println("Experiment complete. Results saved to: " + results.directory());
    }

    private static String getResource(String name) throws IOException {
        final String path = "/sql/r03/" + name;
        return ExperimentUtils.readResource(R03Experiment.class, path);
    }
}
