package interfaces;
import enums.Gender;
import java.sql.*;
import java.time.LocalDate;
import java.util.Properties;
import java.util.UUID;

public class DatabaseConnection implements IDatabaseConnection {

    Connection connection;
    IDatabaseConnection dbConnection;
    private static DatabaseConnection INSTANCE;

    private DatabaseConnection() {

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
                "FOREIGN KEY (customer_id) REFERENCES Customer(id)" +
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

    public void addNewCustomer(Costumer customer) {
        String query = "INSERT INTO customer (id, first_name, last_name, birth_date, gender) "
                + String.format("VALUES ('%s', '%s', '%s' , '%s', '%s');", customer.getId(), customer.getFirstName(), customer.getLastName(), customer.getBirthDate(), customer.getGender());
        executeQuery(query);
    }


    public void addNewReading(Reading reading) {

        String query = "INSERT INTO reading (id, comment, customer_id, date_of_reading, kind_of_meter, meter_count, meter_id, substitute) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Setze die Parameter für die Abfrage
            preparedStatement.setString(1, reading.getId().toString());
            preparedStatement.setString(2, reading.getComment());
            preparedStatement.setString(3, reading.getCustomer().getId().toString());
            preparedStatement.setDate(4, java.sql.Date.valueOf(reading.getDateOfReading())); // assuming it's a LocalDate
            preparedStatement.setString(5, reading.getKindOfMeter().toString());
            preparedStatement.setString(6, reading.getMeterCount().toString());
            preparedStatement.setString(7, reading.getMeterId());
            preparedStatement.setBoolean(8, reading.getSubstitute());

            // Führe die SQL-Abfrage aus
            preparedStatement.executeUpdate();
            System.out.println("Datensatz erfolgreich eingefügt!");

        } catch (SQLException e) {
            System.err.println("Fehler beim Einfügen des Datensatzes: " + e.getMessage());
        }
    }


    public Costumer readCustomer(UUID id) {

        String selectCustomer = "SELECT * FROM customer WHERE id = ?;";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (PreparedStatement statement = connection.prepareStatement(selectCustomer)) {
            statement.setObject(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String x = resultSet.getString("first_name");
                String y = resultSet.getString("last_name");
                LocalDate z = resultSet.getDate("birth_date").toLocalDate();
                Gender gender = Gender.valueOf(resultSet.getString("gender"));

                return new Costumer(id, x, y, z, gender);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    private void executeQuery(String query) {
        try {
            Statement statement = connection.createStatement();
            statement.execute(query);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public Costumer deleteCustomerById(UUID customerId) {
        String deleteCustomerSQL = "DELETE FROM Customer WHERE id = ?;";
        Connection connection1 = DatabaseConnection.getInstance().connection;

        try ( PreparedStatement statement= connection.prepareStatement(deleteCustomerSQL)) {
            statement.setObject(1, customerId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Customer with ID " + customerId + " was deleted successfully.");
            } else {
                System.out.println("No customer found with ID " + customerId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateCustomerById(Costumer costumer) {
        String updateCustomerSQL = "UPDATE Customer SET first_name = ?, last_name = ?, birth_date = ?, gender = ? WHERE id = ?;";

        try (var preparedStatement = connection.prepareStatement(updateCustomerSQL)) {
            preparedStatement.setString(1, costumer.getFirstName());
            preparedStatement.setString(2, costumer.getLastName());
            preparedStatement.setDate(3, java.sql.Date.valueOf(costumer.getBirthDate()));
            preparedStatement.setString(4, costumer.getGender().toString());
            preparedStatement.setString(5, costumer.getId().toString());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Customer with ID " + costumer.getId() + " was updated successfully.");
            } else {
                System.out.println("No customer found with ID " + costumer.getId());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}




