package benchmark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Database {
    public static Connection connect(Config config) throws SQLException {
        final Properties properties = new Properties();

        properties.setProperty("user", config.user());
        properties.setProperty("password", config.password());

        // Prevents an automatic switch to a generic prepared plan between repetitions.
        properties.setProperty("prepareThreshold", "0");

        return DriverManager.getConnection(config.jdbcUrl(), properties);
    }


    public static void executeScript(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    // Formatting for the run context; EXPLAIN plans are saved separately.
    public static String queryText(Connection connection, String sql) throws SQLException {
        StringBuilder text = new StringBuilder();
        try (Statement statement = connection.createStatement(); var rows = statement.executeQuery(sql)) {
            int columns = rows.getMetaData().getColumnCount();
            while (rows.next()) {
                for (int column = 1; column <= columns; column++) {
                    if (column > 1) text.append(" | ");
                    text.append(rows.getString(column));
                }
                text.append('\n');
            }
        }
        return text.toString();
    }
}
