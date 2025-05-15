package hausfix.resourcen;

import hausfix.CRUD.CrudReading;
import hausfix.CRUD.CrudCustomer;
import hausfix.Database.DatabaseConnection;
import hausfix.entities.Customer;
import hausfix.entities.Reading;
import hausfix.enums.Gender;
import hausfix.enums.KindOfMeter;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static hausfix.Main.getProperties;
import static org.junit.jupiter.api.Assertions.*;

public class readingsTest {

    private static Connection connection;
    private static CrudReading crudReading;
    private static CrudCustomer crudCustomer;
    private static Readings readingsResource;

    @BeforeAll
    public static void setUp() throws SQLException {
        // DB-Verbindung öffnen
        DatabaseConnection dbManager = DatabaseConnection.getInstance();
        connection = dbManager.openConnection(getProperties());
        assertNotNull(connection, "Database connection should not be null.");

        // Dummy-User einfügen, um den Foreign-Key in der Customer-Tabelle zu befriedigen.
        // Falls weitere Spalten (z.B. name) als NOT NULL definiert sind, müssten diese hier ebenfalls gesetzt werden.
        try (Statement stmt = connection.createStatement()) {
            // INSERT IGNORE vermeidet einen Fehler, wenn der Dummy-User schon existiert.
            stmt.executeUpdate("INSERT IGNORE INTO users (id) VALUES (1)");
            // Bei deaktiviertem Auto-Commit explizit committen:
            connection.commit();
        } catch (SQLException e) {
            System.out.println("Dummy user with id 1 already exists or could not be inserted: " + e.getMessage());
        }

        crudReading = new CrudReading();
        crudCustomer = new CrudCustomer();
        readingsResource = new Readings();
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        // DB bereinigen (optional)
        DatabaseConnection dbManager = DatabaseConnection.getInstance();
        dbManager.truncateAllTables();
        dbManager.closeConnection();
    }

    // -------------------------------------------------------------------------
    // 1) CREATE
    // -------------------------------------------------------------------------
    @Test
    public void testCreateReadingEndpoint() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "John", "Doe", LocalDate.of(1990, 1, 1), Gender.M);

        UUID readingId = UUID.randomUUID();
        Reading reading = new Reading(
                readingId,
                "Test Reading",
                customer,
                LocalDate.now(),
                KindOfMeter.STROM,
                123.45,
                "METER001",
                false
        );

        Response response = readingsResource.createReading(reading);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Reading createdReading = (Reading) response.getEntity();
        assertNotNull(createdReading.getId(), "Reading ID should be generated (or taken) by the server.");
        assertEquals("Test Reading", createdReading.getComment());
    }

    @Test
    public void testCreateReadingEndpointWithNullId() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "NullID", "Test", LocalDate.of(2000, 1, 1), Gender.W);

        Reading reading = new Reading(
                null,
                "Reading with null ID",
                customer,
                LocalDate.now(),
                KindOfMeter.HEIZUNG,
                321.0,
                "METER-NULL",
                false
        );

        Response response = readingsResource.createReading(reading);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        Reading createdReading = (Reading) response.getEntity();
        assertNotNull(createdReading.getId(), "ID should have been generated on the server side.");
        assertEquals("Reading with null ID", createdReading.getComment());
    }

    // -------------------------------------------------------------------------
    // 2) UPDATE
    // -------------------------------------------------------------------------
    /*
    @Test
    public void testUpdateReadingEndpoint() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "Jane", "Update", LocalDate.of(1992, 2, 2), Gender.W);
        Reading reading = new Reading(
                UUID.randomUUID(),
                "Original Comment",
                customer,
                LocalDate.now(),
                KindOfMeter.WASSER,
                567.89,
                "METER002",
                true
        );

        // Zuerst anlegen
        readingsResource.createReading(reading);

        // Kommentar ändern
        reading.setComment("Updated Comment");
       // Jetzt mit PathParam aufrufen:
        String idString = reading.getId().toString();
        Response response = readingsResource.updateReading(idString, reading);
                // Wir erwarten 204 No Content (keinen Body)
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertNull(response.getEntity(), "Bei 204 No Content liefert der Body null.");
    }

     */

    // -------------------------------------------------------------------------
    // 3) DELETE
    // -------------------------------------------------------------------------
    @Test
    public void testDeleteReadingEndpoint() {
        UUID readingId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "Jane", "Delete", LocalDate.of(1985, 5, 15), Gender.W);

        Reading reading = new Reading(
                readingId,
                "Reading to Delete",
                customer,
                LocalDate.now(),
                KindOfMeter.STROM,
                123.45,
                "METER001",
                false
        );

        readingsResource.createReading(reading);

        Response response = readingsResource.deleteReading(readingId.toString());
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());

        // deleteReadingById(...) gibt immer null zurück => also hier:
        Reading deletedReading = (Reading) response.getEntity();
        assertNull(deletedReading, "deleteReadingById(...) returns null by design => resource also returns null");
    }

    // -------------------------------------------------------------------------
    // 4) GET by ID
    // -------------------------------------------------------------------------
    @Test
    public void testGetReadingByIdEndpoint() {
        UUID readingId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "John", "ById", LocalDate.of(1990, 5, 15), Gender.M);
        Reading reading = new Reading(
                readingId,
                "Existing Reading",
                customer,
                LocalDate.now(),
                KindOfMeter.WASSER,
                456.78,
                "METER003",
                true
        );

        // Anlegen
        readingsResource.createReading(reading);

        // Abrufen
        Response response = readingsResource.getReadingById(readingId.toString());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        Reading retrievedReading = (Reading) response.getEntity();
        assertNotNull(retrievedReading, "Should find the reading we just created.");
        assertEquals(readingId, retrievedReading.getId(), "IDs must match.");
    }

    @Test
    public void testGetReadingByIdNotFound() {
        UUID randomId = UUID.randomUUID();
        Response response = readingsResource.getReadingById(randomId.toString());
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus(),
                "We expect 404 if that reading does not exist in DB.");
    }

    // -------------------------------------------------------------------------
    // 5) GET All Readings
    // -------------------------------------------------------------------------
    /**
     * Normaler GET-all ohne Filter:
     */
    @Test
    public void testGetAllReadingsNoParams() {
        // Alle Parameter null: userId, customer, start, end, kindOfMeter
        Response response = readingsResource.getAllReadings(null, null, null, null, null);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity(), "Should return a list of all readings.");
    }

    /**
     * Beispiel: Ungültiges "kindOfMeter" => 400 BAD_REQUEST
     */
    @Test
    public void testGetAllReadingsInvalidKindOfMeter() {
        // userId und customer null, start und end null, kindOfMeter = "INVALID_KIND"
        Response response = readingsResource.getAllReadings(null, null, null, null, "INVALID_KIND");

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        String errorMsg = (String) response.getEntity();
        assertTrue(errorMsg.contains("Invalid kindOfMeter value"),
                "We expect an error message about invalid kindOfMeter.");
    }

    /**
     * Beispiel: 400 if StartDate > EndDate
     */
    @Test
    public void testGetAllReadingsStartAfterEnd() {
        // userId und customer null, start = "2050-01-01" und end = "2049-12-31", kindOfMeter null
        Response response = readingsResource.getAllReadings(null, null, "2050-01-01", "2049-12-31", null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        String errorMsg = (String) response.getEntity();
        assertTrue(errorMsg.contains("Start date cannot be after end date."),
                "We expect an error about start/end range.");
    }

    /**
     * Test: explizit nach Customer-ID filtern
     */
    @Test
    public void testGetAllReadingsWithCustomerFilter() {
        // 1) Random Customer
        UUID randomCustomerId = UUID.randomUUID();
        Customer myCustomer = new Customer(
                randomCustomerId,
                "Filtered",
                "Customer",
                LocalDate.of(1999, 9, 9),
                Gender.W
        );
        // 2) Reading anlegen
        UUID readingId = UUID.randomUUID();
        Reading reading = new Reading(
                readingId,
                "Filtered by customer",
                myCustomer,
                LocalDate.now(),
                KindOfMeter.HEIZUNG,
                10.0,
                "METER-FILT",
                false
        );
        readingsResource.createReading(reading);

        // 3) Filtern nach customer (userId bleibt null)
        Response response = readingsResource.getAllReadings(null, randomCustomerId, null, null, null);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        @SuppressWarnings("unchecked")
        List<Reading> filteredReadings = (List<Reading>) response.getEntity();
        assertNotNull(filteredReadings, "Should return a list of matched readings.");

        // Prüfen, ob unser Reading gefunden wird.
        boolean found = filteredReadings.stream()
                .anyMatch(r -> readingId.equals(r.getId()));
        assertTrue(found, "We should find our newly created reading by matching customer ID filter.");
    }

    /**
     * Test: Filtern nach CustomerID, die NICHTS matcht => erwarte leere Liste
     */
    @Test
    public void testGetAllReadingsWithCustomerFilterNoMatch() {
        // Zufällige Customer-ID, die nicht angelegt wurde.
        UUID randomCustomerId = UUID.randomUUID();

        Response response = readingsResource.getAllReadings(null, randomCustomerId, null, null, null);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        @SuppressWarnings("unchecked")
        List<Reading> filteredReadings = (List<Reading>) response.getEntity();
        assertNotNull(filteredReadings);

        assertTrue(filteredReadings.isEmpty(),
                "If no reading has this random customerId, the result should be empty.");
    }

    /**
     * Test: Filtern nach UserId (Long)
     * Da der userId-Filter verwendet wird, sollten andere Filter ignoriert werden.
     * In diesem Test gehen wir davon aus, dass es keine Readings für die gegebene userId gibt.
     */
    @Test
    public void testGetAllReadingsWithUserIdFilter() {
        Long userId = 100L;

        // Erwartung: Da keine Reading explizit mit userId verbunden wurde, sollte eine leere Liste zurückgegeben werden.
        Response response = readingsResource.getAllReadings(userId, null, null, null, null);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        @SuppressWarnings("unchecked")
        List<Reading> readingsForUser = (List<Reading>) response.getEntity();
        assertNotNull(readingsForUser, "Should return a list (even if empty) for userId filter.");
        assertTrue(readingsForUser.isEmpty(), "No readings should be returned for userId 100L.");
    }
}
