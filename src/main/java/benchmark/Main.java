package benchmark;

import benchmark.experiments.R01Experiment;
import benchmark.experiments.R02Experiment;
import benchmark.experiments.R03Experiment;
import benchmark.experiments.R04Experiment;
import benchmark.experiments.R05Experiment;
import benchmark.experiments.R06Experiment;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Config config = Config.fromEnvironment();

        try (final Scanner input = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                System.out.println("\nSelect an experiment:");
                System.out.println("1. R01 — cardinality estimates");
                System.out.println("2. R02 — selective filter: compare without/with a customer index");
                System.out.println("3. R03 — single-column versus composite index");
                System.out.println("4. R04 — function in a filter and expression index");
                System.out.println("5. R05 — sorting with LIMIT");
                System.out.println("6. R06 — repeated lookups in a join");
                System.out.println("0. Exit");
                System.out.print("> ");
                if (!input.hasNextLine()) return;
                String option = input.nextLine().trim();
                switch (option) {
                    case "0" -> { return; }
                    case "1" -> {
                        System.out.println("Dataset: 1,000,000 customers with correlated country and currency values.");
                        new R01Experiment(config).run();
                    }
                    case "2" -> {
                        System.out.println("Dataset: 1,000,000 orders; target customer with 100 matches.");
                        new R02Experiment(config).run();
                    }
                    case "3" -> {
                        System.out.println("Dataset: 1,000,000 orders; 20,000 for the target customer, of which 20 are pending.");
                        new R03Experiment(config).run();
                    }
                    case "4" -> {
                        System.out.println("Dataset: 1,000,000 customers with unique emails.");
                        new R04Experiment(config).run();
                    }
                    case "5" -> {
                        System.out.println("Dataset: 1,000,000 orders; the query returns the 10 most recent.");
                        new R05Experiment(config).run();
                    }
                    case "6" -> {
                        System.out.println("Dataset: 1,000 customers and 100,000 orders; 100 totals calculated.");
                        new R06Experiment(config).run();
                    }
                    default -> System.out.println("Invalid option.");
                }
            }
        }
    }
}
