package benchmark;

import java.nio.file.Path;
import java.util.Map;

public record Config(String host, int port, String user, String password, Path results) {
    static Config fromEnvironment() {
        Map<String, String> env = System.getenv();
        int port = Integer.parseInt(env.getOrDefault("BENCHMARK_DB_PORT", "55432"));

        // Defaults match the database defined in compose.yaml.
        return new Config(
                env.getOrDefault("BENCHMARK_DB_HOST", "localhost"),
                port,
                env.getOrDefault("BENCHMARK_DB_USER", "benchmark"),
                env.getOrDefault("BENCHMARK_DB_PASSWORD", "benchmark_local"),
                Path.of(env.getOrDefault("BENCHMARK_RESULTS_DIR", "results"))
        );
    }

    String jdbcUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/benchmark_experiments";
    }
}
