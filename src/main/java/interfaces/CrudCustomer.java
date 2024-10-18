package interfaces;
import enums.Gender;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

public class CrudCustomer {

    public void addNewCustomer(Costumer customer) {
        String query = "INSERT INTO customer (id, first_name, last_name, birth_date, gender) VALUES (?, ?, ?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, customer.getId().toString());
            preparedStatement.setString(2, customer.getFirstName());
            preparedStatement.setString(3, customer.getLastName());
            preparedStatement.setString(4, customer.getBirthDate().toString());
            preparedStatement.setString(5, customer.getGender().toString());

            preparedStatement.executeUpdate();

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


    public void updateCustomerById(Costumer costumer) {
        String updateCustomerSQL = "UPDATE Customer SET first_name = ?, last_name = ?, birth_date = ?, gender = ? WHERE id = ?;";
        Connection connection = DatabaseConnection.getInstance().connection;

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


    public Costumer deleteCustomerById(UUID customerId) {
        String deleteCustomerSQL = "DELETE FROM Customer WHERE id = ?;";
        Connection connection = DatabaseConnection.getInstance().connection;

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
}
