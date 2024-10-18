package interfaces;
import enums.Gender;
import java.sql.*;
import java.time.LocalDate;
import java.util.Properties;
import java.util.UUID;

public class DatabaseConnection implements IDatabaseConnection {

    public Connection connection;
    IDatabaseConnection dbConnection;
    private static DatabaseConnection INSTANCE;

    public DatabaseConnection() {

    }


    public static DatabaseConnection getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DatabaseConnection();
        }
        return INSTANCE;
    }


    @Override
    public IDatabaseConnection openConnection(Properties properties) {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String pw = properties.getProperty("db.pw");


        try {
            connection = DriverManager.getConnection(url, user, pw);
        } catch (SQLException e) {
            System.out.println(e);
        }
        System.out.println("connection established");
        return dbConnection;
    }


    @Override
    public void createAllTables() {

        String createCustomerTableSQL = "CREATE TABLE IF NOT EXISTS Customer (" +
                "id UUID PRIMARY KEY," +
                "first_name VARCHAR(255) NOT NULL," +
                "last_name VARCHAR(255) NOT NULL," +
                "birth_date DATE NOT NULL," +
                "gender ENUM('D', 'M', 'U', 'W') NOT NULL);";

        String createReadingTableSQL = "CREATE TABLE IF NOT EXISTS Reading (" +
                "id UUID PRIMARY KEY," +
                "comment VARCHAR(255)," +
                "customer_id UUID," +
                "date_of_reading DATE," +
                "kind_of_meter ENUM('HEIZUNG', 'STROM', 'WASSER', 'UNBEKANNT') NOT NULL," +
                "meter_count DOUBLE," +
                "meter_id VARCHAR(255)," +
                "substitute BOOLEAN," +
                "FOREIGN KEY (customer_id) REFERENCES Customer(id) ON DELETE SET NULL" +
                ")";

        try (Statement statement = connection.createStatement()) {

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
        String truncateCustomerTableSQL = "TRUNCATE TABLE Customer;";
        String truncateReadingTableSQL = "TRUNCATE TABLE Reading;";

        try (Statement statement = connection.createStatement()) {

            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");

            statement.executeUpdate(truncateReadingTableSQL);
            System.out.println("Reading table truncated successfully.");

            statement.executeUpdate(truncateCustomerTableSQL);
            System.out.println("Customer table truncated successfully.");

            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void removeAllTables() {
        String dropCustomerTableSQL = "DROP TABLE IF EXISTS customer;";
        String dropReadingTableSQL = "DROP TABLE IF EXISTS reading;";

        try (Statement statement = connection.createStatement()) {

            statement.executeUpdate(dropReadingTableSQL);
            System.out.println("Reading table dropped successfully.");

            statement.executeUpdate(dropCustomerTableSQL);
            System.out.println("Costumer table dropped successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connection closed successfully");
            } catch (SQLException e) {
                System.err.println("Error while closing the connection: " + e.getMessage());
            }
        } else {
            System.out.println("Connection is already closed");
        }
    }
}




