package hausfix.Database;

import hausfix.interfaces.IDatabaseConnection;
import hausfix.entities.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection implements IDatabaseConnection {

    public Connection connection;
    private static DatabaseConnection INSTANCE;

    public DatabaseConnection() {}

    public static DatabaseConnection getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DatabaseConnection();
        }
        return INSTANCE;
    }

    @Override
    public Connection openConnection(Properties properties) {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String pw = properties.getProperty("db.pw");
        try {
            connection = DriverManager.getConnection(url, user, pw);
            System.out.println("connection established");
        } catch (SQLException e) {
            System.out.println("Error opening connection: " + e.getMessage());
        }
        return connection;
    }

    @Override
    public void createAllTables() {
        // Wenn keine Connection, Abbruch
        if (connection == null) {
            System.out.println("createAllTables() aufgerufen, aber connection == null. Abbruch.");
            return;
        }

        // users-Tabelle in Kleinbuchstaben
        String createUserTableSQL = "CREATE TABLE IF NOT EXISTS `users` ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "username VARCHAR(255) NOT NULL UNIQUE,"
                + "password VARCHAR(255) NOT NULL"
                + ");";

        String createCustomerTableSQL = "CREATE TABLE IF NOT EXISTS Customer ("
                + "id UUID PRIMARY KEY,"
                + "first_name VARCHAR(255) NOT NULL,"
                + "last_name VARCHAR(255) NOT NULL,"
                + "birth_date DATE NOT NULL,"
                + "gender ENUM('D', 'M', 'U', 'W') NOT NULL,"
                + "user_id BIGINT,"
                + "FOREIGN KEY (user_id) REFERENCES `users`(id) ON DELETE SET NULL"
                + ");";

        String createReadingTableSQL = "CREATE TABLE IF NOT EXISTS Reading ("
                + "id UUID PRIMARY KEY,"
                + "comment VARCHAR(255),"
                + "customer_id UUID,"
                + "date_of_reading DATE,"
                + "kind_of_meter ENUM('HEIZUNG', 'STROM', 'WASSER', 'UNBEKANNT') NOT NULL,"
                + "meter_count DOUBLE,"
                + "meter_id VARCHAR(255),"
                + "substitute BOOLEAN,"
                + "user_id BIGINT,"  // Hier wird die Spalte definiert
                + "FOREIGN KEY (customer_id) REFERENCES Customer(id) ON DELETE SET NULL,"
                + "FOREIGN KEY (user_id) REFERENCES `users`(id) ON DELETE SET NULL"
                + ");";

        // Ausführung der SQL-Befehle
        try (Statement statement = connection.createStatement()) {
            statement.execute(createUserTableSQL);
            System.out.println("`users` table created.");

            statement.execute(createCustomerTableSQL);
            System.out.println("Customer table created.");

            statement.execute(createReadingTableSQL);
            System.out.println("Reading table created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void truncateAllTables() {
        if (connection == null) {
            System.out.println("truncateAllTables() -> connection == null. Abbruch.");
            return;
        }

        try (Statement statement = connection.createStatement()) {
            // Prüfen, ob es H2 oder z. B. MySQL ist
            if (!connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("H2")) {
                // Nicht-H2: TRUNCATE + FOREIGN_KEY_CHECKS
                statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");
                statement.executeUpdate("TRUNCATE TABLE READING;");
                statement.executeUpdate("TRUNCATE TABLE CUSTOMER;");
                statement.executeUpdate("TRUNCATE TABLE `users`;");
                statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");
            } else {
                // H2: DELETE
                statement.executeUpdate("DELETE FROM READING;");
                statement.executeUpdate("DELETE FROM CUSTOMER;");
                statement.executeUpdate("DELETE FROM `users`;");
            }
            System.out.println("Tables truncated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeAllTables() {
        if (connection == null) {
            System.out.println("removeAllTables() -> connection == null. Abbruch.");
            return;
        }

        String dropReadingTableSQL = "DROP TABLE IF EXISTS Reading;";
        String dropCustomerTableSQL = "DROP TABLE IF EXISTS Customer;";
        String dropUserTableSQL = "DROP TABLE IF EXISTS `users`;";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(dropReadingTableSQL);
            System.out.println("Reading table dropped successfully.");

            statement.executeUpdate(dropCustomerTableSQL);
            System.out.println("Customer table dropped successfully.");

            statement.executeUpdate(dropUserTableSQL);
            System.out.println("`users` table dropped successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Connection closed successfully");
            } catch (SQLException e) {
                System.err.println("Error while closing the connection: " + e.getMessage());
            }
        } else {
            System.out.println("Connection is already closed");
        }
    }

    public <T> void save(T entity) {
        if (connection == null) {
            System.out.println("save() aufgerufen, aber connection == null. Abbruch.");
            return;
        }

        try {
            // JDBC-Logik für User speichern
            if (entity instanceof User) {
                User user = (User) entity;
                String sql = "INSERT INTO `users` (username, password) VALUES (?, ?)";

                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, user.getUsername());
                    preparedStatement.setString(2, user.getPassword());
                    preparedStatement.executeUpdate();
                    System.out.println("User erfolgreich gespeichert.");
                }
            } else {
                System.out.println("Unbekannte Entität: " + entity.getClass().getSimpleName());
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Speichern der Entität: " + e.getMessage());
        }
    }
}
