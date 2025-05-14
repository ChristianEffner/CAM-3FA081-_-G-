package hausfix.resourcen;
import hausfix.CRUD.CrudCustomer;
import hausfix.Database.DatabaseConnection;
import hausfix.entities.User;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;


import static hausfix.Main.getProperties;
import static org.junit.jupiter.api.Assertions.*;

    public class ImportResourceTest {

        private final ImportResource importResource = new ImportResource();
        private static DatabaseConnection dbManager;
        private static Connection connection;
        private static CrudCustomer crudCustomer;
        private static ExportResource exportResource;

        @BeforeAll
        static void setUp() throws SQLException {
            dbManager = DatabaseConnection.getInstance();
            connection = dbManager.openConnection(getProperties());
            crudCustomer = new CrudCustomer();

            exportResource = new ExportResource();
        }

        @AfterAll
        static void tearDown() throws SQLException {
            dbManager.closeConnection();
        }

        @Test
        void testImportCsvSuccess() {
            String csvContent = """
                First Name,Last Name,Birth Date,Gender,User ID
                Alper,Testmann,1990-01-01,M,1
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
            FormDataContentDisposition fileDetail = FormDataContentDisposition.name("file").fileName("customers.csv").build();

            Response response = importResource.importCustomers(inputStream, fileDetail, "csv");
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Import erfolgreich"));
        }

        @Test
        void testImportJsonSuccess() {
            String jsonContent = """
                [{
                  "firstName": "Alper",
                  "lastName": "Testmann",
                  "birthDate": "1990-01-01",
                  "gender": "M",
                  "userId": 1
                }]
                """;
            InputStream inputStream = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
            FormDataContentDisposition fileDetail = FormDataContentDisposition.name("file").fileName("customers.json").build();

            Response response = importResource.importCustomers(inputStream, fileDetail, "json");
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Import erfolgreich"));
        }

        @Test
        void testImportXmlSuccess() throws SQLException {
            // 1. Benutzer anlegen, damit userId gültig ist
            String uniqueUsername = "xmlTestUser_" + UUID.randomUUID();
            User user = new User(uniqueUsername, "securePass");
            Users usersResource = new Users();
            Response userResponse = usersResource.createUser(user);

            // Fehlerbehandlung hinzufügen:
            assertEquals(Response.Status.CREATED.getStatusCode(), userResponse.getStatus(),
                    "Benutzer konnte nicht angelegt werden");

            Object userEntity = userResponse.getEntity();
            assertTrue(userEntity instanceof User, "Response-Entity ist kein User");

            User createdUser = (User) userEntity;
            Long userId = createdUser.getId();

            // 2. XML mit gültiger userId erzeugen
            String xmlContent = """
        <customers>
            <customer>
                <firstName>Alper</firstName>
                <lastName>Testmann</lastName>
                <birthDate>1990-01-01</birthDate>
                <gender>M</gender>
                <userId>%d</userId>
            </customer>
        </customers>
        """.formatted(userId);

            // 3. InputStream und Dateidetail vorbereiten
            InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
            FormDataContentDisposition fileDetail = FormDataContentDisposition
                    .name("file")
                    .fileName("customers.xml")
                    .build();

            // 4. Import aufrufen
            Response response = importResource.importCustomers(inputStream, fileDetail, "xml");

            // 5. Ergebnis prüfen
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Import erfolgreich"));
        }


        @Test
        void testImportWithMissingParams() {
            Response response = importResource.importCustomers(null, null, null);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Fehlende Datei"));
        }

        @Test
        void testImportWithUnknownFormat() {
            InputStream dummy = new ByteArrayInputStream("dummy".getBytes());
            FormDataContentDisposition fileDetail = FormDataContentDisposition.name("file").fileName("dummy.txt").build();

            Response response = importResource.importCustomers(dummy, fileDetail, "unknown");
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Ungültiges Importformat"));
        }

        @Test
        void testImportEmptyCsvFile() {
            String emptyCsv = "First Name,Last Name,Birth Date,Gender,User ID\n"; // nur Header
            InputStream inputStream = new ByteArrayInputStream(emptyCsv.getBytes(StandardCharsets.UTF_8));
            FormDataContentDisposition fileDetail = FormDataContentDisposition.name("file").fileName("empty.csv").build();

            Response response = importResource.importCustomers(inputStream, fileDetail, "csv");
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Kein Kunde"));
        }

        @Test
        void testImportJsonWithException() {
            String invalidJson = """ 
                [ { "firstName": "Alper"   // ungültiges JSON
                """;
            InputStream inputStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
            FormDataContentDisposition fileDetail = FormDataContentDisposition.name("file").fileName("bad.json").build();

            Response response = importResource.importCustomers(inputStream, fileDetail, "json");
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Fehler beim Import"));
        }
    }

