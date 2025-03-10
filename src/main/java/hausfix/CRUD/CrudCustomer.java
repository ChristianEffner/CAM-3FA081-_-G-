package hausfix.CRUD;

import hausfix.Database.DatabaseConnection;
import hausfix.entities.Customer;
import hausfix.enums.Gender;
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

    /**
     * Fügt einen Customer in die DB ein, inkl. user_id-Spalte.
     */
    public Response addNewCustomer(Customer customer) throws SQLException {
        // Nun auch user_id
        String query = "INSERT INTO customer (id, first_name, last_name, birth_date, gender, user_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, customer.getId().toString());
            preparedStatement.setString(2, customer.getFirstName());
            preparedStatement.setString(3, customer.getLastName());
            preparedStatement.setDate(4, java.sql.Date.valueOf(customer.getBirthDate()));
            preparedStatement.setString(5, customer.getGender().toString());

            // Falls userId nicht gesetzt, bleibts 0 (oder wirf Fehler)
            if (customer.getUserId() == null) {
                preparedStatement.setNull(6, java.sql.Types.BIGINT);
            } else {
                preparedStatement.setLong(6, customer.getUserId());
            }

            preparedStatement.executeUpdate(); // Führe das Insert aus

            return Response.status(Response.Status.CREATED)
                    .entity(customer)
                    .build();
        } catch (SQLException e) {
            if (e.getMessage().contains("Cannot be null")) {
                throw new SQLException("Null value not allowed for customer fields: " + e.getMessage(), e);
            }
            throw e;
        }
    }

    /**
     * Liest alle Customers (ohne Filter).
     */
    public List<Customer> readAllCustomers() {
        String selectCustomer = "SELECT * FROM customer;";
        Connection connection = DatabaseConnection.getInstance().connection;
        List<Customer> customers = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(selectCustomer)) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                customers.add(mapRowToCustomer(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    /**
     * NEU: Liest nur die Customers eines bestimmten Users (user_id).
     */
    public List<Customer> readCustomersForUser(Long userId) {
        String sql = "SELECT * FROM customer WHERE user_id = ?";
        List<Customer> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().connection;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRowToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Findet einen Kunden anhand seiner ID (UUID).
     */
    public Customer readCustomer(UUID id) {
        String selectCustomer = "SELECT * FROM customer WHERE id = ?;";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (PreparedStatement statement = connection.prepareStatement(selectCustomer)) {
            statement.setObject(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return mapRowToCustomer(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Aktualisiert einen Kunden.
     */
    public Response updateCustomerById(Customer customer) {
        String updateCustomerSQL =
                "UPDATE Customer SET first_name = ?, last_name = ?, birth_date = ?, gender = ?, user_id = ? "
                        + "WHERE id = ?;";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateCustomerSQL)) {
            preparedStatement.setString(1, customer.getFirstName());
            preparedStatement.setString(2, customer.getLastName());
            preparedStatement.setDate(3, java.sql.Date.valueOf(customer.getBirthDate()));
            preparedStatement.setString(4, customer.getGender().toString());

            if (customer.getUserId() == null) {
                preparedStatement.setNull(5, java.sql.Types.BIGINT);
            } else {
                preparedStatement.setLong(5, customer.getUserId());
            }

            preparedStatement.setString(6, customer.getId().toString());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Customer with ID " + customer.getId() + " was updated successfully.");
                return Response.ok("Customer updated").build();
            } else {
                System.out.println("No customer found with ID " + customer.getId());
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Customer not found")
                        .build();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating the customer: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Löscht einen Kunden anhand der ID.
     */
    public Customer deleteCustomerById(UUID customerId) {
        String deleteCustomerSQL = "DELETE FROM Customer WHERE id = ?;";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (PreparedStatement statement= connection.prepareStatement(deleteCustomerSQL)) {
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

    /**
     * Hilfsfunktion, um einen ResultSet-Datensatz in ein Customer-Objekt zu mappen.
     */
    private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        LocalDate birthDate = rs.getDate("birth_date").toLocalDate();
        Gender gender = Gender.valueOf(rs.getString("gender"));

        Customer c = new Customer(id, firstName, lastName, birthDate, gender);

        // user_id aus DB
        long userIdLong = rs.getLong("user_id");
        if (!rs.wasNull()) {
            c.setUserId(userIdLong);
        }
        return c;
    }
}
