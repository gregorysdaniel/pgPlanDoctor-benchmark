package benchmark.experiments;

import benchmark.Config;
import benchmark.Database;
import benchmark.ExperimentUtils;
import benchmark.Results;

import java.io.IOException;
import java.sql.Connection;

public class R06Experiment {
    static final int MAX_CUSTOMER = 100;

    private final Config config;

    public R06Experiment(Config config) {
        this.config = config;
    }

    public void run() throws Exception {
        final Results results = new Results(config.results(), "r06");
        System.out.println("Results for this run: " + results.directory());

        results.record("experiment", "R06 — repeated access on the inner side of a join");
        results.record("hypothesis", "An index matching the join condition may reduce the work of each inner lookup.");
        results.record("dataset", "1,000 customers; 100,000 orders; 100 customers queried");
        results.record("parameter", "customer.id <= " + MAX_CUSTOMER);
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

            System.out.println("Preparing data without an index on orders.customer_id...");
            Database.executeScript(connection, setup);
            ExperimentUtils.recordIndexes(connection, results, "benchmark_r06", "orders", "before");
            ExperimentUtils.runCondition(
                    connection, query, "before", "BEFORE — WITHOUT JOIN INDEX", results, MAX_CUSTOMER);

            System.out.println("Creating the index on orders.customer_id...");
            Database.executeScript(connection, optimization);
            ExperimentUtils.recordIndexes(connection, results, "benchmark_r06", "orders", "after");
            ExperimentUtils.runCondition(
                    connection, query, "after", "AFTER — WITH JOIN INDEX", results, MAX_CUSTOMER);
        }

        System.out.println("Experiment complete. Results saved to: " + results.directory());
    }

    private static String getResource(String name) throws IOException {
        final String path = "/sql/r06/" + name;
        return ExperimentUtils.readResource(R06Experiment.class, path);
    }
}
