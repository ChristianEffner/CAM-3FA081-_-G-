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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static hausfix.Main.getProperties;
import static org.junit.jupiter.api.Assertions.*;

public class readingsTest {

    private static Connection connection;
    private static CrudReading crudReading;
    private static CrudCustomer crudCustomer;
    private static readings readingsResource;

    @BeforeAll
    public static void setUp() throws SQLException {
        // DB-Verbindung öffnen
        DatabaseConnection dbManager = DatabaseConnection.getInstance();
        connection = dbManager.openConnection(getProperties());
        assertNotNull(connection, "Database connection should not be null.");

        crudReading = new CrudReading();
        crudCustomer = new CrudCustomer();
        readingsResource = new readings();
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

        // Erst anlegen
        readingsResource.createReading(reading);

        // Kommentar ändern
        reading.setComment("Updated Comment");
        Response response = readingsResource.updateReading(reading);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Reading updated", response.getEntity());
    }

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
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        // Dein deleteReadingById(...) gibt immer null zurück => also hier:
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


    /**
     * Normaler GET-all ohne Filter:
     */
    @Test
    public void testGetAllReadingsNoParams() {
        // => 1.Param = null (keine Customer-UUID),
        //    2. & 3. Param = null (kein Start/End),
        //    4. Param = null (kein KindOfMeter).
        Response response = readingsResource.getAllReadings(null, null, null, null);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity(), "Should return a list of all readings.");
    }

    /**
     * Beispiel: Ungültiges "kindOfMeter" => 400 BAD_REQUEST
     */
    @Test
    public void testGetAllReadingsInvalidKindOfMeter() {
        // -> 1.Param = null => kein Customer,
        //    2. & 3. = null => kein Datumsfilter,
        //    4. = "INVALID_KIND"
        Response response = readingsResource.getAllReadings(null, null, null, "INVALID_KIND");

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        String errorMsg = (String) response.getEntity();
        // Die readings.java schickt -> "Invalid kindOfMeter value. Please provide a valid value."
        assertTrue(errorMsg.contains("Invalid kindOfMeter value"),
                "We expect an error message about invalid kindOfMeter.");
    }

    /**
     * Beispiel: 400 if StartDate > EndDate
     */
    @Test
    public void testGetAllReadingsStartAfterEnd() {
        // => 1. Param = null => kein Customer
        // => 2. & 3. Param => "2050-01-01" und "2049-12-31"
        // => 4. Param => null
        Response response = readingsResource.getAllReadings(null, "2050-01-01", "2049-12-31", null);

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

        // 3) Nun filtern wir GENAU nach randomCustomerId (== 1. Param)
        //    rest = null => kein Datumsfilter, kein kindOfMeter
        Response response = readingsResource.getAllReadings(
                randomCustomerId, // ACHTUNG => Hier jetzt NICHT .toString()
                null,
                null,
                null
        );
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        @SuppressWarnings("unchecked")
        List<Reading> filteredReadings = (List<Reading>) response.getEntity();
        assertNotNull(filteredReadings, "Should return a list of matched readings.");

        // Prüfen
        boolean found = filteredReadings.stream()
                .anyMatch(r -> readingId.equals(r.getId()));
        assertTrue(found, "We should find our newly created reading by matching customer ID filter.");
    }

    /**
     * Test: Filtern nach CustomerID, die NICHTS matcht => erwarte leere Liste
     */
    @Test
    public void testGetAllReadingsWithCustomerFilterNoMatch() {
        // x-beliebige UUID, die wir nicht angelegt haben
        UUID randomCustomerId = UUID.randomUUID();

        Response response = readingsResource.getAllReadings(
                randomCustomerId, // 1. param = "unknown" Customer
                null,
                null,
                null
        );
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        @SuppressWarnings("unchecked")
        List<Reading> filteredReadings = (List<Reading>) response.getEntity();
        assertNotNull(filteredReadings);

        assertTrue(filteredReadings.isEmpty(),
                "If no reading has this random customerId, the result should be empty.");
    }
}
