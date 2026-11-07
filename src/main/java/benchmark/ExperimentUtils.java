package benchmark;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ExperimentUtils {
    private static final int WARMUPS = 1;
    private static final int MEASUREMENTS = 5;

    private static final String SESSION_SQL = """
            SET max_parallel_workers_per_gather = 0;
            SET jit = off;
            """;

    public static String protocol() {
        return WARMUPS + " warmup and " + MEASUREMENTS + " measurements per condition";
    }

    public static void configureSession(Connection connection) throws Exception {
        Database.executeScript(connection, SESSION_SQL);
    }

    public static void recordEnvironment(Connection connection, Results results) throws Exception {
        results.record("postgresql", Database.queryText(connection, "SELECT version()"));
        results.record("settings", Database.queryText(connection, """
                SELECT name, setting FROM pg_settings
                WHERE name IN ('max_parallel_workers_per_gather', 'jit') ORDER BY name
                """));
    }

    public static void recordIndexes(
            Connection connection,
            Results results,
            String schema,
            String table,
            String condition
    ) throws Exception {
        results.record("indexes_" + condition, Database.queryText(connection, """
                SELECT indexname, indexdef FROM pg_indexes
                WHERE schemaname = '%s' AND tablename = '%s' ORDER BY indexname
                """.formatted(schema, table)));
    }

    public static void runCondition(
            Connection connection,
            String query,
            String file,
            String condition,
            Results results,
            Object... parameters
    ) throws Exception {
        try (BufferedWriter writer = results.openFile(file);
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (int index = 0; index < parameters.length; index++) {
                statement.setObject(index + 1, parameters[index]);
            }

            for (int run = 1; run <= WARMUPS + MEASUREMENTS; run++) {
                final String label = run <= WARMUPS
                        ? "WARMUP " + run
                        : "MEASUREMENT " + (run - WARMUPS);
                printAndSave(writer, "=== " + condition + " | " + label + " ===");

                try (var rows = statement.executeQuery()) {
                    while (rows.next()) printAndSave(writer, rows.getString(1));
                }

                printAndSave(writer, "");
            }
        }
    }

    private static void printAndSave(BufferedWriter file, String line) throws IOException {
        file.write(line);
        file.write('\n');
        System.out.println(line);
    }

    public static String readResource(Class<?> owner, String path) throws IOException {
        try (var input = owner.getResourceAsStream(path)) {
            if (input == null) throw new IOException("Script not found: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
