package hausfix.CRUD;
import hausfix.Database.DatabaseConnection;
import hausfix.entities.Customer;
import hausfix.entities.Reading;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;
import hausfix.enums.Gender;
import hausfix.enums.KindOfMeter;
import java.sql.*;
import static hausfix.Main.getProperties;
import static org.junit.jupiter.api.Assertions.*;


class CrudReadingTest {


    private static CrudCustomer crudCustomer;
    private static CrudReading crudReading;
    private static Connection connection;

    @BeforeAll
    public static void setUp() throws SQLException {
        DatabaseConnection dbManager = DatabaseConnection.getInstance();
        connection = dbManager.openConnection(getProperties());
        crudCustomer = new CrudCustomer();
        crudReading = new CrudReading();
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        DatabaseConnection dbManager = new DatabaseConnection();
        connection = dbManager.openConnection(getProperties());
        dbManager.truncateAllTables();
        dbManager.closeConnection();
    }



    @Test
    public void testAddNewReadingSuccess() throws SQLException {

        // Setup: Einen neuen Kunden und ein Reading erstellen
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "John", "Doe", LocalDate.of(1990, 1, 1), Gender.M);
        Reading reading = new Reading(UUID.randomUUID(), "Test Comment", customer, LocalDate.now(), KindOfMeter.STROM, 123.45, "METER001", false);

        crudCustomer.addNewCustomer(customer);
        crudReading.addNewReading(reading);

        // Verify the reading was added
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM reading WHERE id = ?")) {
            stmt.setString(1, reading.getId().toString());
            ResultSet resultSet = stmt.executeQuery();

            assertTrue(resultSet.next(), "Reading should exist in the database.");
            assertEquals("Test Comment", resultSet.getString("comment"));
            assertEquals(customerId.toString(), resultSet.getString("customer_id"));
            assertEquals(LocalDate.now(), resultSet.getDate("date_of_reading").toLocalDate());
            assertEquals("STROM", resultSet.getString("kind_of_meter"));
            assertEquals(123.45, resultSet.getDouble("meter_count"), 0.001);
            assertEquals("METER001", resultSet.getString("meter_id"));
            assertFalse(resultSet.getBoolean("substitute"));
        }
    }

    @Test
    public void testAddNewReadingWithNonExistentCustomer() throws SQLException {

        // Setup: Ein Reading mit einem neuen Kunden erstellen, der noch nicht in der DB ist
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "Jane", "Smith", LocalDate.of(1985, 5, 15), Gender.W);
        Reading reading = new Reading(UUID.randomUUID(), "Another Test Comment", customer, LocalDate.now(), KindOfMeter.WASSER, 678.90, "METER002", true);

        crudReading.addNewReading(reading);

        // Verify the customer and reading were added
        Customer retrievedCustomer = crudCustomer.readCustomer(customerId);
        assertNotNull(retrievedCustomer, "Customer should be added if not existing.");

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM reading WHERE id = ?")) {
            stmt.setString(1, reading.getId().toString());
            ResultSet resultSet = stmt.executeQuery();

            assertTrue(resultSet.next(), "Reading should exist in the database.");
            assertEquals("Another Test Comment", resultSet.getString("comment"));
            assertEquals(customerId.toString(), resultSet.getString("customer_id"));
            assertEquals(LocalDate.now(), resultSet.getDate("date_of_reading").toLocalDate());
            assertEquals("WASSER", resultSet.getString("kind_of_meter"));
            assertEquals(678.90, resultSet.getDouble("meter_count"), 0.001);
            assertEquals("METER002", resultSet.getString("meter_id"));
            assertTrue(resultSet.getBoolean("substitute"));
        }
    }

    @Test
    public void testReadReadingSuccess() throws SQLException {

        // Setup: Ein Reading mit einer bekannten ID hinzufügen
        UUID readingId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "John", "Doe", LocalDate.of(1990, 1, 1), Gender.M);
        Reading reading = new Reading(readingId, "Test Comment", customer, LocalDate.now(), KindOfMeter.STROM, 123.45, "METER001", false);

        // Add customer and reading to the database
        crudCustomer.addNewCustomer(customer);
        crudReading.addNewReading(reading);

        // Act: Das Reading mit der ID abrufen
        Reading retrievedReading = crudReading.readReading(readingId);

        // Assert: Überprüfen, ob das Reading korrekt abgerufen wurde
        assertNotNull(retrievedReading, "Reading should be retrieved successfully.");
        assertEquals(readingId, retrievedReading.getId(), "Reading ID should match.");
        assertEquals("Test Comment", retrievedReading.getComment(), "Reading comment should match.");
        assertEquals(customerId, retrievedReading.getCustomer().getId(), "Customer ID should match.");
        assertEquals(123.45, retrievedReading.getMeterCount(), 0.001, "Meter count should match.");
        assertEquals("METER001", retrievedReading.getMeterId(), "Meter ID should match.");
        assertEquals(false, retrievedReading.getSubstitute(), "Substitute flag should match.");
    }


    @Test
    public void testReadReadingNotFound() throws SQLException {

        // Setup: Eine ungültige UUID für das Reading
        UUID invalidReadingId = UUID.randomUUID();

        // Act: Das nicht existierende Reading versuchen zu lesen
        Reading retrievedReading = crudReading.readReading(invalidReadingId);

        // Assert: Das Ergebnis sollte null sein, weil das Reading nicht existiert
        assertNull(retrievedReading, "Reading should not be found.");
    }


    @Test
    public void testDeleteReadingByIdSuccess() throws SQLException {

        // Setup: Ein neues Reading in die Datenbank einfügen
        UUID readingId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "John", "Doe", LocalDate.of(1990, 1, 1), Gender.M);
        Reading reading = new Reading(readingId, "Test Comment", customer, LocalDate.now(), KindOfMeter.STROM, 123.45, "METER001", false);

        // Reading in die Datenbank einfügen
        crudCustomer.addNewCustomer(customer);
        crudReading.addNewReading(reading);

        // Act: Das Reading mit der ID löschen
        crudReading.deleteReadingById(readingId);

        // Assert: Überprüfen, ob das Reading aus der Datenbank gelöscht wurde
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM reading WHERE id = ?")) {
            stmt.setString(1, readingId.toString());
            ResultSet resultSet = stmt.executeQuery();

            assertFalse(resultSet.next(), "Reading should be deleted and not exist in the database.");
        }
    }


    @Test
    public void testDeleteReadingByIdNotFound() throws SQLException {

        // Setup: Eine ungültige UUID für das Reading
        UUID invalidReadingId = UUID.randomUUID();

        // Act: Versuchen, das nicht existierende Reading zu löschen
        crudReading.deleteReadingById(invalidReadingId);

        // Assert: Überprüfen, dass keine Fehler auftreten, aber das Reading existiert nicht mehr
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM reading WHERE id = ?")) {
            stmt.setString(1, invalidReadingId.toString());
            ResultSet resultSet = stmt.executeQuery();

            assertFalse(resultSet.next(), "No reading should exist in the database for the invalid ID.");
        }
    }


    @Test
    public void testUpdateReadingByIdSuccess() throws SQLException {

        // Setup: Ein neues Reading in die Datenbank einfügen
        UUID readingId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "John", "Doe", LocalDate.of(1990, 1, 1), Gender.M);
        Reading reading = new Reading(readingId, "Old Comment", customer, LocalDate.now(), KindOfMeter.STROM, 123.45, "METER001", false);

        // Reading in die Datenbank einfügen
        crudCustomer.addNewCustomer(customer);
        crudReading.addNewReading(reading);

        // Update: Das Reading mit neuen Werten aktualisieren
        Reading updatedReading = new Reading(readingId, "Updated Comment", customer, LocalDate.now().plusDays(1), KindOfMeter.WASSER, 200.75, "METER002", true);
        crudReading.updateReadingById(updatedReading);

        // Assert: Überprüfen, ob das Reading in der Datenbank aktualisiert wurde
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM reading WHERE id = ?")) {
            stmt.setString(1, readingId.toString());
            ResultSet resultSet = stmt.executeQuery();

            assertTrue(resultSet.next(), "Reading should exist in the database.");
            assertEquals("Updated Comment", resultSet.getString("comment"));
            assertEquals(customerId.toString(), resultSet.getString("customer_id"));
            assertEquals(updatedReading.getDateOfReading(), resultSet.getDate("date_of_reading").toLocalDate());
            assertEquals("WASSER", resultSet.getString("kind_of_meter"));
            assertEquals(200.75, resultSet.getDouble("meter_count"), 0.001);
            assertEquals("METER002", resultSet.getString("meter_id"));
            assertTrue(resultSet.getBoolean("substitute"));
        }
    }


    @Test
    public void testUpdateReadingByIdNotFound() throws SQLException {

        // Setup: Eine ungültige UUID für das Reading
        UUID invalidReadingId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "Jane", "Smith", LocalDate.of(1985, 5, 15), Gender.W);
        Reading reading = new Reading(invalidReadingId, "Test Comment", customer, LocalDate.now(), KindOfMeter.STROM, 678.90, "METER002", false);

        // Act: Versuchen, das nicht existierende Reading zu aktualisieren
        crudReading.updateReadingById(reading);

        // Assert: Überprüfen, dass das Reading nicht in der Datenbank existiert
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM reading WHERE id = ?")) {
            stmt.setString(1, invalidReadingId.toString());
            ResultSet resultSet = stmt.executeQuery();

            assertFalse(resultSet.next(), "No reading should exist in the database for the invalid ID.");
        }
    }

}