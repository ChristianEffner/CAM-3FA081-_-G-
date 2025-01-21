package hausfix.CRUD;
import hausfix.entities.Customer;
import hausfix.enums.Gender;
import hausfix.Database.DatabaseConnection;
import jakarta.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrudCustomer {

    public Response addNewCustomer(Customer customer) throws SQLException {
        String query = "INSERT INTO customer (id, first_name, last_name, birth_date, gender) VALUES (?, ?, ?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, customer.getId().toString());
            preparedStatement.setString(2, customer.getFirstName());
            preparedStatement.setString(3, customer.getLastName());
            preparedStatement.setDate(4, java.sql.Date.valueOf(customer.getBirthDate()));
            preparedStatement.setString(5, customer.getGender().toString());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return Response.status(Response.Status.CREATED) // Status 201 Created
                        .entity(customer) // Den neu erstellten Kunden zurückgeben
                        .build();
            } else {
                // Wenn keine Zeilen betroffen sind, ein Fehlerstatus zurückgeben
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to create customer")
                        .build();
            }
        } catch (SQLException e) {
            // Fehlerbehandlung und Weiterwerfen der SQLException
            throw new SQLException("Error while creating customer: " + e.getMessage(), e);
        }
    }


    public List<Customer> readAllCustomers() {
        String selectCustomer = "SELECT * FROM customer;";
        Connection connection = DatabaseConnection.getInstance().connection;
        List<Customer> customers = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(selectCustomer)) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                UUID id = UUID.fromString(resultSet.getString("id"));
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                LocalDate birthDate = resultSet.getDate("birth_date").toLocalDate();
                Gender gender = Gender.valueOf(resultSet.getString("gender"));

                Customer customer = new Customer(id, firstName, lastName, birthDate, gender);
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }


    public Customer readCustomer(UUID id) {

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

                return new Customer(id, x, y, z, gender);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public Response updateCustomerById(Customer customer) {
        String updateCustomerSQL = "UPDATE Customer SET first_name = ?, last_name = ?, birth_date = ?, gender = ? WHERE id = ?;";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (var preparedStatement = connection.prepareStatement(updateCustomerSQL)) {
            preparedStatement.setString(1, customer.getFirstName());
            preparedStatement.setString(2, customer.getLastName());
            preparedStatement.setDate(3, java.sql.Date.valueOf(customer.getBirthDate()));
            preparedStatement.setString(4, customer.getGender().toString());
            preparedStatement.setString(5, customer.getId().toString());

            int rowsAffected = preparedStatement.executeUpdate();

            // Wenn keine Zeilen betroffen sind, existiert der Kunde nicht
            if (rowsAffected > 0) {
                // Erfolgreiche Aktualisierung
                System.out.println("Customer with ID " + customer.getId() + " was updated successfully.");
                return Response.ok("Customer updated").build();
            } else {
                // Kunde nicht gefunden
                System.out.println("No customer found with ID " + customer.getId());
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Customer not found")
                        .build();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating the customer")
                    .build();
        }
    }


    public Customer deleteCustomerById(UUID customerId) {
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
