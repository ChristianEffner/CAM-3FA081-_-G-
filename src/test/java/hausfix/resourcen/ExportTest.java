package hausfix.resourcen;

import hausfix.Database.DatabaseConnection;
import hausfix.CRUD.CrudCustomer;
import hausfix.entities.Customer;
import hausfix.enums.Gender;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static hausfix.Main.getProperties;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExportTest {

    private DatabaseConnection dbManager;
    private Connection connection;
    private CrudCustomer crudCustomer;
    private ExportResource exportResource;

    @BeforeAll
    void setUp() throws SQLException {
        dbManager = DatabaseConnection.getInstance();
        connection = dbManager.openConnection(getProperties());
        crudCustomer = new CrudCustomer();

        exportResource = new ExportResource();
    }

    @AfterAll
    void tearDown() throws SQLException {
        dbManager.closeConnection();
    }

    @Test
    void testExportCustomersCsv() throws SQLException {

        Response response = exportResource.exportCustomers("csv");
        assertEquals(200, response.getStatus());
        assertEquals("text/csv", response.getHeaderString("Content-Type"));
        assertTrue(response.getHeaderString("Content-Disposition").contains("customers.csv"));

        String csv = (String) response.getEntity();
        assertNotNull(csv);
        assertTrue(csv.contains("Alper"));
        System.out.println("CSV:\n" + csv);
    }

    @Test
    void testExportCustomersJson() throws SQLException {


        Response response = exportResource.exportCustomers("json");
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getHeaderString("Content-Type"));

        String json = (String) response.getEntity();
        assertNotNull(json);
        System.out.println("JSON:\n" + json);
    }

    @Test
    void testExportCustomersXml() throws SQLException {


        Response response = exportResource.exportCustomers("xml");
        assertEquals(200, response.getStatus());
        assertEquals("application/xml", response.getHeaderString("Content-Type"));

        String xml = (String) response.getEntity();
        assertNotNull(xml);
        System.out.println("XML:\n" + xml);
    }

    @Test
    void testExportCustomersInvalidFormat() {
        Response response = exportResource.exportCustomers("html");
        assertEquals(400, response.getStatus());
        String error = (String) response.getEntity();
        assertTrue(error.contains("Ungültiges Format"));
    }
}
