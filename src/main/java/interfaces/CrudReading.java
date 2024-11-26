package interfaces;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


public class CrudReading extends DatabaseConnection {

    private CrudCustomer crudCustomer = new CrudCustomer();

    public void addNewReading(Reading reading) {
        String query = "INSERT INTO reading (id, comment, customer_id, date_of_reading, kind_of_meter, meter_count, meter_id, substitute) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().connection;

        try {
            UUID customerId = reading.getCustomer().getId();
            Customer existingCustomer = crudCustomer.readCustomer(customerId);

            if (existingCustomer == null) {
                ICustomer iCustomer = reading.getCustomer();

                // Cast to Costumer, if possible
                if (iCustomer instanceof Customer) {
                    Customer newCustomer = (Customer) iCustomer;

                    crudCustomer.addNewCustomer(newCustomer);
                }
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                // Set the parameters for the query
                preparedStatement.setString(1, reading.getId().toString());
                preparedStatement.setString(2, reading.getComment());
                preparedStatement.setString(3, reading.getCustomer().getId().toString());
                preparedStatement.setDate(4, java.sql.Date.valueOf(reading.getDateOfReading())); // assuming it's a LocalDate
                preparedStatement.setString(5, reading.getKindOfMeter().toString());
                preparedStatement.setDouble(6, reading.getMeterCount()); // setDouble for DOUBLE value
                preparedStatement.setString(7, reading.getMeterId());
                preparedStatement.setBoolean(8, reading.getSubstitute());

                preparedStatement.executeUpdate();
                System.out.println("Record successfully inserted!");

            } catch (SQLException e) {
                System.err.println("Error inserting record: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error adding a new customer: " + e.getMessage());
        }
    }

    public Reading readReading(UUID id) {
        String selectReading = "SELECT * FROM reading WHERE id = ?;";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (PreparedStatement statement = connection.prepareStatement(selectReading)) {
            statement.setObject(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String comment = resultSet.getString("comment");
                String cust_id = resultSet.getString("customer_id");
                String date_of_reading = resultSet.getString("date_of_reading");
                String kind_of_meter = resultSet.getString("kind_of_meter");
                String meter_count = resultSet.getString("meter_count");
                String meter_id = resultSet.getString("meter_id");
                String substitute = resultSet.getString("substitute");

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Reading deleteReadingById(UUID readingId) {
        String deleteReadingSQL = "DELETE FROM Reading WHERE id = ?;";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (PreparedStatement statement= connection.prepareStatement(deleteReadingSQL)) {
            statement.setObject(1, readingId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Reading with ID " + readingId + " was deleted successfully.");
            } else {
                System.out.println("No reading found with ID " + readingId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateReadingById(Reading reading) {
        String updateReadingSQL = "UPDATE Reading SET comment = ?, customer_id = ?, date_of_reading = ?, kind_of_meter = ?, meter_count = ?, meter_id = ?, substitute = ? WHERE id = ?;";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (var preparedStatement = connection.prepareStatement(updateReadingSQL)) {

            preparedStatement.setString(1, reading.getComment());
            preparedStatement.setString(2, reading.getCustomer().getId().toString());
            preparedStatement.setDate(3, java.sql.Date.valueOf(reading.getDateOfReading()));
            preparedStatement.setString(4, reading.getKindOfMeter().toString());
            preparedStatement.setDouble(5, reading.getMeterCount());
            preparedStatement.setString(6, reading.getMeterId());
            preparedStatement.setBoolean(7, reading.getSubstitute());
            preparedStatement.setObject(8, reading.getId());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Reading with ID " + reading.getId() + " was updated successfully.");
            } else {
                System.out.println("No reading found with ID " + reading.getId());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
