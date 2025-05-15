package hausfix.resourcen;
import com.fasterxml.jackson.databind.ObjectMapper;
import hausfix.Database.DatabaseConnection;
import hausfix.CRUD.CrudCustomer;
import hausfix.entities.Customer;
import hausfix.enums.Gender;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.junit.jupiter.api.*;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static hausfix.Main.getProperties;

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
        // 1. Testkunde anlegen
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName("Alper");
        customer.setLastName("Testmann");
        customer.setBirthDate(LocalDate.of(1990, 1, 1));
        customer.setGender(Gender.M);
        customer.setUserId(null);

        // 2. In DB speichern
        crudCustomer.addNewCustomer(customer);

        // 3. Dann Export aufrufen
        Response response = exportResource.exportCustomers("csv");

        // 4. Ausgabe prüfen
        String csv = (String) response.getEntity();
        System.out.println("Exportierter CSV-Inhalt:\n" + csv);
        assertTrue(csv.contains("Alper"));
    }


    @Test
    void testExportCustomersJson() throws SQLException, Exception {
        // 1. Testkunde anlegen
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName("Alper");
        customer.setLastName("Testmann");
        customer.setBirthDate(LocalDate.of(1990, 1, 1));
        customer.setGender(Gender.M);
        customer.setUserId(null);

        // 2. Kunde speichern
        crudCustomer.addNewCustomer(customer);

        // 3. JSON-Export aufrufen
        Response response = exportResource.exportCustomers("json");

        // 4. Response prüfen
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getHeaderString("Content-Type"));

        // 5. Entity ist List<Customer>, daher casten
        Object entity = response.getEntity();
        assertNotNull(entity);
        assertTrue(entity instanceof List<?>);

        // 6. JSON serialisieren (mit JavaTimeModule für LocalDate)
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String json = mapper.writeValueAsString(entity);

        // 7. Ausgabe prüfen
        System.out.println("JSON:\n" + json);
        assertTrue(json.contains("Alper"));
        assertTrue(json.contains("Testmann"));
        assertTrue(json.contains("1990-01-01")); // LocalDate korrekt formatiert
    }

    @Test
    void testExportCustomersXml() throws Exception {
        // 1. Testkunde anlegen
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName("Alper");
        customer.setLastName("Testmann");
        customer.setBirthDate(LocalDate.of(1990, 1, 1));
        customer.setGender(Gender.M);
        customer.setUserId(null);

        crudCustomer.addNewCustomer(customer);

        // 2. Export als XML aufrufen
        Response response = exportResource.exportCustomers("xml");

        // 3. Status und Header prüfen
        assertEquals(200, response.getStatus());
        assertEquals("application/xml", response.getHeaderString("Content-Type"));

        // 4. Entity auslesen (ist ein CustomerList-Objekt)
        Object entity = response.getEntity();
        assertNotNull(entity);
        assertTrue(entity instanceof CustomerList);

        CustomerList customerList = (CustomerList) entity;

        // 5. Mit JAXB in XML-String serialisieren
        JAXBContext jaxbContext = JAXBContext.newInstance(CustomerList.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter writer = new StringWriter();
        marshaller.marshal(customerList, writer);
        String xml = writer.toString();

        // 6. XML-Ausgabe prüfen
        System.out.println("XML:\n" + xml);
        assertTrue(xml.contains("Alper"));
        assertTrue(xml.contains("Testmann"));
    }

    @Test
    void testExportCustomersInvalidFormat() {
        Response response = exportResource.exportCustomers("html");
        assertEquals(400, response.getStatus());
        String error = (String) response.getEntity();
        assertTrue(error.contains("Ungültiges Format"));
    }
}
