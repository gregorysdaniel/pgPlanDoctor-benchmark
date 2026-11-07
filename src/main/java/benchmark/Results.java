package benchmark;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Results {
    private final Path directory;

    public Results(Path root, String experiment) throws IOException {
        Files.createDirectories(root);

        final String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        directory = Files.createDirectory(root.resolve(experiment + "-" + timestamp));

        record("started_at_utc", Instant.now().toString());
    }

    public Path directory() { return directory.toAbsolutePath(); }

    public void record(String key, String value) throws IOException {
        Files.writeString(directory.resolve("context.txt"), key + "=\n" + value + "\n\n",
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public void save(String name, String text) throws IOException {
        Files.writeString(directory.resolve(name), text, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

    public BufferedWriter openFile(final String fileName) throws IOException {
        return Files.newBufferedWriter(directory.resolve(fileName + ".txt"), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW);
    }

}
