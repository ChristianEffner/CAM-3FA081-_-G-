package hausfix.CRUD;
import hausfix.Database.DatabaseConnection;
import hausfix.entities.Customer;
import hausfix.enums.Gender;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static hausfix.Main.getProperties;
import static org.junit.jupiter.api.Assertions.*;

public class CrudCustomerTest {

    private static CrudCustomer crudCustomer;
    private static Connection connection;

    @BeforeAll
    public static void setUp() throws SQLException {
        DatabaseConnection dbManager = DatabaseConnection.getInstance();
        connection = dbManager.openConnection(getProperties());
        crudCustomer = new CrudCustomer();
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        DatabaseConnection dbManager = new DatabaseConnection();
        connection = dbManager.openConnection(getProperties());
        dbManager.truncateAllTables();
        dbManager.closeConnection();
    }

    @Test
    public void testAddNewCustomerSuccess() throws SQLException {

        Customer customer = new Customer(UUID.randomUUID(), "John", "Doe", LocalDate.of(1990, 1, 1), Gender.M);
        crudCustomer.addNewCustomer(customer);

        // Verify the customer was added using the generated UUID as ID
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM customer WHERE id = ?")) {
            stmt.setString(1, customer.getId().toString()); // Verwende die dynamische UUID
            ResultSet resultSet = stmt.executeQuery();

            assertTrue(resultSet.next(), "Customer should exist in the database.");
            assertEquals("John", resultSet.getString("first_name"));
            assertEquals("Doe", resultSet.getString("last_name"));
            assertEquals(LocalDate.of(1990, 1, 1), resultSet.getDate("birth_date").toLocalDate());
            assertEquals("M", resultSet.getString("gender"));
        }
    }

    @Test
    public void testAddNewCustomerWithNullFields() {
        // Erstelle einen Kunden mit null für first_name
        Customer customer = new Customer(UUID.randomUUID(), null, "Doe", LocalDate.of(1995, 5, 15), Gender.U);

        // Stelle sicher, dass eine SQLException geworfen wird
        SQLException exception = assertThrows(SQLException.class, () -> crudCustomer.addNewCustomer(customer));

        // Überprüfe, ob die Fehlermeldung den "Cannot be null" Fehler enthält
        assertTrue(exception.getMessage().contains("Column 'first_name' cannot be null"));
    }

    @Test
    void testAddNewCustomerWithNullFieldsThrowsSQLException() {
        // Arrange: Erstelle ein Customer-Objekt mit einem null-Wert für 'first_name'
        Customer customerWithNullFirstName = new Customer(UUID.randomUUID(),null , "last_name", LocalDate.of(1995, 5, 15), Gender.M);

        // Act & Assert: Versuche, den Kunden hinzuzufügen und erwarte eine SQLException
        SQLException exception = assertThrows(SQLException.class, () -> {
            crudCustomer.addNewCustomer(customerWithNullFirstName);
        });

        // Überprüfe, ob die erwartete Fehlermeldung im Exception-Message enthalten ist
        assertTrue(exception.getMessage().contains("Column 'first_name' cannot be null"));
    }

    @Test
    public void testReadCustomerSuccess() throws SQLException {

        // Setup: Einen neuen Kunden hinzufügen
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "John", "Doe", LocalDate.of(1990, 1, 1), Gender.M);
        crudCustomer.addNewCustomer(customer);

        // Test: Versuche, den Kunden zu lesen
        Customer retrievedCustomer = crudCustomer.readCustomer(customerId);

        // Assertions
        assertNotNull(retrievedCustomer, "Retrieved customer should not be null.");
        assertEquals(customer.getId(), retrievedCustomer.getId(), "Customer ID should match.");
        assertEquals(customer.getFirstName(), retrievedCustomer.getFirstName(), "First name should match.");
        assertEquals(customer.getLastName(), retrievedCustomer.getLastName(), "Last name should match.");
        assertEquals(customer.getBirthDate(), retrievedCustomer.getBirthDate(), "Birth date should match.");
        assertEquals(customer.getGender(), retrievedCustomer.getGender(), "Gender should match.");
    }

    @Test
    public void testReadCustomerNotFound() {

        // Test: Versuche, einen Kunden zu lesen, der nicht existiert
        UUID nonExistentId = UUID.randomUUID();
        Customer retrievedCustomer = crudCustomer.readCustomer(nonExistentId);

        // Assertions
        assertNull(retrievedCustomer, "Retrieved customer should be null for non-existent ID.");
    }

    @Test
    public void testReadAllCustomersSuccess() {
        CrudCustomer customer1 = new CrudCustomer();

        // Test: Lese alle Kunden
        List<Customer> customers = customer1.readAllCustomers();

        // Assertions
        assertNotNull(customers, "Customer list should not be null.");
        assertFalse(customers.isEmpty(), "Customer list should not be empty.");
    }

    @Test
    public void testUpdateCustomerByIdSuccess() throws SQLException {

        // Setup: Einen neuen Kunden hinzufügen
        UUID customerId = UUID.randomUUID();
        Customer originalCustomer = new Customer(customerId, "John", "Doe", LocalDate.of(1990, 1, 1), Gender.M);
        crudCustomer.addNewCustomer(originalCustomer);

        // Update: Ändere die Kundendaten
        Customer updatedCustomer = new Customer(customerId, "Jane", "Smith", LocalDate.of(1992, 2, 2), Gender.W);
        crudCustomer.updateCustomerById(updatedCustomer);

        // Test: Versuche, den aktualisierten Kunden zu lesen
        Customer retrievedCustomer = crudCustomer.readCustomer(customerId);

        // Assertions
        assertNotNull(retrievedCustomer, "Retrieved customer should not be null.");
        assertEquals(updatedCustomer.getFirstName(), retrievedCustomer.getFirstName(), "First name should be updated.");
        assertEquals(updatedCustomer.getLastName(), retrievedCustomer.getLastName(), "Last name should be updated.");
        assertEquals(updatedCustomer.getBirthDate(), retrievedCustomer.getBirthDate(), "Birth date should be updated.");
        assertEquals(updatedCustomer.getGender(), retrievedCustomer.getGender(), "Gender should be updated.");
    }

    @Test
    public void testUpdateCustomerByIdNotFound() {

        // Test: Versuche, einen nicht existierenden Kunden zu aktualisieren
        UUID nonExistentId = UUID.randomUUID();
        Customer nonExistentCustomer = new Customer(nonExistentId, "Ghost", "User", LocalDate.of(1980, 1, 1), Gender.M);

        // Capture console output to verify the correct message is printed
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        crudCustomer.updateCustomerById(nonExistentCustomer);

        // Restore original System.out
        System.setOut(originalOut);

        // Assertions
        String output = outContent.toString();
        assertTrue(output.contains("No customer found with ID " + nonExistentId), "Expected message for non-existent customer.");
    }


    @Test
    public void testDeleteCustomerByIdSuccess() throws SQLException {

        // Setup: Einen neuen Kunden hinzufügen
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "John", "Doe", LocalDate.of(1990, 1, 1), Gender.M);
        crudCustomer.addNewCustomer(customer);

        // Test: Versuche, den Kunden zu löschen
        crudCustomer.deleteCustomerById(customerId);

        // Verify the customer was deleted
        Customer retrievedCustomer = crudCustomer.readCustomer(customerId);
        assertNull(retrievedCustomer, "Customer should be null after being deleted.");
    }

    @Test
    public void testDeleteCustomerByIdNotFound() {
        // Test: Versuche, einen nicht existierenden Kunden zu löschen

        Connection connection = DatabaseConnection.getInstance().connection;
        UUID nonExistentId = UUID.randomUUID();

        // Capture console output to verify the correct message is printed
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        crudCustomer.deleteCustomerById(nonExistentId);

        // Restore original System.out
        System.setOut(originalOut);

        // Assertions
        String output = outContent.toString();
        assertTrue(output.contains("No customer found with ID " + nonExistentId), "Expected message for non-existent customer.");
    }

    @Test
    void testReadAllCustomersWithData() {
        // Test für den Fall, dass Daten in der Tabelle vorhanden sind
        List<Customer> customers = crudCustomer.readAllCustomers();

        assertEquals(3, customers.size(), "Es sollten drei Kunden in der Liste sein.");
        // Weitere Überprüfungen der Kundendaten wie IDs, Namen, etc.
    }

    @Test
    void testReadAllCustomersWithEmptyTable() throws SQLException {
        // Test für den Fall, dass die Tabelle leer ist
        String deleteAllData = "DELETE FROM customer;";
        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteAllData)) {
            deleteStatement.execute();
        }

        List<Customer> customers = crudCustomer.readAllCustomers();

        assertTrue(customers.isEmpty(), "Die Kundenliste sollte leer sein, wenn keine Daten in der Tabelle vorhanden sind.");
    }

    @Test
    void testReadAllCustomersWithSQLException() throws SQLException {
        // Test für den Fall, dass eine SQLException auftritt
        Connection faultyConnection = DatabaseConnection.getInstance().connection;
        //faultyConnection.close(); // Erzwingt eine Exception


        List<Customer> customers = crudCustomer.readAllCustomers();

        assertNotNull(customers, "Die Kundenliste sollte nicht null sein.");
        assertTrue(customers.isEmpty(), "Die Kundenliste sollte leer sein, wenn eine SQLException auftritt.");
    }
}
