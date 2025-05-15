package hausfix.resourcen;
import hausfix.Database.DatabaseConnection;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.jupiter.api.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.UUID;

class ImportReadingsResourceTest {

    private ImportReadingsResource resource;
    private final UUID testCustomerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @BeforeAll
    static void setupDatabase() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        DatabaseConnection.getInstance().connection = conn;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE users (
                    id BIGINT PRIMARY KEY
                );
            """);
            stmt.execute("INSERT INTO users (id) VALUES (1);");

            stmt.execute("""
                CREATE TABLE customer (
                    id UUID PRIMARY KEY,
                    first_name VARCHAR(100),
                    last_name VARCHAR(100),
                    birth_date DATE,
                    gender VARCHAR(10),
                    user_id BIGINT
                );
            """);

            stmt.execute("""
                CREATE TABLE reading (
                    id UUID PRIMARY KEY,
                    comment VARCHAR(255),
                    customer_id UUID,
                    date_of_reading DATE,
                    kind_of_meter VARCHAR(50),
                    meter_count DOUBLE,
                    meter_id VARCHAR(100),
                    substitute BOOLEAN,
                    user_id BIGINT
                );
            """);
        }
    }

    @BeforeEach
    void cleanAndInit() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().connection;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM reading;");
            stmt.execute("DELETE FROM customer;");
        }

        insertDummyCustomer();
        resource = new ImportReadingsResource();
    }

    void insertDummyCustomer() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().connection;

        try (PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO customer (id, first_name, last_name, birth_date, gender, user_id)
            VALUES (?, ?, ?, ?, ?, ?)
        """)) {
            ps.setObject(1, testCustomerId);
            ps.setString(2, "Max");
            ps.setString(3, "Mustermann");
            ps.setDate(4, Date.valueOf("1990-01-01"));
            ps.setString(5, "M");
            ps.setLong(6, 1L);
            ps.executeUpdate();
        }
    }

    @Test
    void testValidCSVWithExistingCustomer() {
        String csv = """
            Customer ID,Date,Value,KindOfMeter,MeterId,Comment
            123e4567-e89b-12d3-a456-426614174000,2023-05-10,120.5,WASSER,M123,Testkommentar
            """;

        InputStream input = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        FormDataContentDisposition disposition = FormDataContentDisposition.name("file").fileName("data.csv").build();

        Response response = resource.importReadings(input, disposition, "csv");
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertTrue(response.getEntity().toString().contains("Import erfolgreich"));
    }

    @Test
    void testMissingParameters() {
        Response response = resource.importReadings(null, null, null);
        Assertions.assertEquals(400, response.getStatus());
        Assertions.assertTrue(response.getEntity().toString().contains("Fehlende Datei"));
    }

    @Test
    void testInvalidFormat() {
        InputStream input = new ByteArrayInputStream("dummy".getBytes(StandardCharsets.UTF_8));
        FormDataContentDisposition disposition = FormDataContentDisposition.name("file").fileName("dummy.txt").build();

        Response response = resource.importReadings(input, disposition, "wrongformat");
        Assertions.assertEquals(400, response.getStatus());
        Assertions.assertTrue(response.getEntity().toString().contains("Ungültiges Format"));
    }

    @Test
    void testEmptyCSV() {
        String csv = "Customer ID,Date,Value,KindOfMeter,MeterId,Comment\n";
        InputStream input = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        FormDataContentDisposition disposition = FormDataContentDisposition.name("file").fileName("empty.csv").build();

        Response response = resource.importReadings(input, disposition, "csv");
        Assertions.assertEquals(400, response.getStatus());
        Assertions.assertTrue(response.getEntity().toString().contains("Keine Ablesedaten"));
    }

    @Test
    void testValidJSONImport() {
        String json = """
        [
            {
                "customer": { "id": "123e4567-e89b-12d3-a456-426614174000" },
                "dateOfReading": "2023-05-12",
                "meterCount": 123.4,
                "kindOfMeter": "WASSER",
                "meterId": "M999",
                "comment": "JSON-Test"
            }
        ]
        """;

        InputStream input = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        FormDataContentDisposition disposition = FormDataContentDisposition.name("file").fileName("data.json").build();

        Response response = resource.importReadings(input, disposition, "json");
        Assertions.assertEquals(200, response.getStatus());
    }

    @Test
    void testInvalidJSONFormat() {
        String json = "[{ invalid json }";
        InputStream input = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        FormDataContentDisposition disposition = FormDataContentDisposition.name("file").fileName("bad.json").build();

        Response response = resource.importReadings(input, disposition, "json");
        Assertions.assertEquals(500, response.getStatus());
        Assertions.assertTrue(response.getEntity().toString().contains("Fehler beim Import"));
    }
}





