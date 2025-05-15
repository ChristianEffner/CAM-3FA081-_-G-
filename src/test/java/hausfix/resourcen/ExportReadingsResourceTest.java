package hausfix.resourcen;
import hausfix.Database.DatabaseConnection;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ExportReadingsResourceTest {


        private ExportReadingsResource resource;
        private final UUID testCustomerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        @BeforeAll
        static void setupDatabase() throws SQLException {
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb_export;DB_CLOSE_DELAY=-1");
            DatabaseConnection.getInstance().connection = conn;

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE users (id BIGINT PRIMARY KEY);");
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
        void init() throws SQLException {
            Connection conn = DatabaseConnection.getInstance().connection;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM reading;");
                stmt.execute("DELETE FROM customer;");
            }

            insertDummyCustomer();
            insertDummyReading();
            resource = new ExportReadingsResource();
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

        void insertDummyReading() throws SQLException {
            Connection conn = DatabaseConnection.getInstance().connection;
            try (PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO reading (id, comment, customer_id, date_of_reading, kind_of_meter, meter_count, meter_id, substitute, user_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """)) {
                ps.setObject(1, UUID.randomUUID());
                ps.setString(2, "Kommentar");
                ps.setObject(3, testCustomerId);
                ps.setDate(4, Date.valueOf(LocalDate.of(2025, 1, 12)));
                ps.setString(5, "WASSER");
                ps.setDouble(6, 123.45);
                ps.setString(7, "M001");
                ps.setBoolean(8, false);
                ps.setLong(9, 1L);
                ps.executeUpdate();
            }
        }

        @Test
        void testExportAsJson() {
            Response response = resource.exportReadings("json", null);
            assertEquals(200, response.getStatus());
            assertEquals("application/json", response.getMediaType().toString());
        }

        @Test
        void testExportAsXml() {
            Response response = resource.exportReadings("xml", null);
            assertEquals(200, response.getStatus());
            assertEquals("application/xml", response.getMediaType().toString());
            assertTrue(response.getHeaders().getFirst("Content-Disposition").toString().contains("readings.xml"));
        }

        @Test
        void testExportAsCsv() {
            Response response = resource.exportReadings("csv", null);
            assertEquals(200, response.getStatus());
            assertEquals("text/csv", response.getMediaType().toString());
            assertTrue(response.getEntity().toString().contains("Customer ID"));
        }

        @Test
        void testExportInvalidFormat() {
            Response response = resource.exportReadings("html", null);
            assertEquals(400, response.getStatus());
            assertTrue(response.getEntity().toString().contains("Ungültiges Format"));
        }

        @Test
        void testExportForSpecificUser() {
            Response response = resource.exportReadings("json", 1L);
            assertEquals(200, response.getStatus());
        }

        @Test
        void testExportWithEmptyResult() throws SQLException {
            // lösche alle readings → leere Liste erwartet
            Connection conn = DatabaseConnection.getInstance().connection;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM reading;");
            }

            Response response = resource.exportReadings("json", null);
            assertEquals(204, response.getStatus());
        }
    }


