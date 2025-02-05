package hausfix.resourcen;

import hausfix.Database.DatabaseConnection;
import hausfix.CRUD.CrudCustomer;
import hausfix.entities.Customer;
import hausfix.enums.Gender;
import hausfix.rest.RestCustomer;
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
class customersTest {

    private DatabaseConnection dbManager;
    private Connection connection;
    private CrudCustomer crudCustomer;
    private customers customerResource;

    @BeforeAll
    void setUp() throws SQLException {
        // Verwende die Instanzvariable, nicht eine lokale Variable
        dbManager = DatabaseConnection.getInstance();
        connection = dbManager.openConnection(getProperties());
        crudCustomer = new CrudCustomer();
        customerResource = new customers(crudCustomer);
    }

    @AfterAll
    void tearDown() throws SQLException {
        dbManager.closeConnection();
    }

    @Test
    void testCreateCustomer() throws SQLException {
        // Erstellen eines neuen Kunden
        Customer newCustomer = new Customer();
        newCustomer.setFirstName("John");
        newCustomer.setLastName("Doe");
        newCustomer.setBirthDate(LocalDate.of(1990, 1, 1));
        newCustomer.setGender(Gender.M);

        // REST-Objekt mit dem neuen Kunden (RestCustomer wird hier als einfacher Wrapper angenommen)
        Response response = customerResource.createCustomer(new RestCustomer(newCustomer));

        // Überprüfen der Antwort
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Customer createdCustomer = (Customer) response.getEntity();
        assertNotNull(createdCustomer.getId());

        // Überprüfen, ob der Kunde in der Datenbank existiert
        Customer dbCustomer = crudCustomer.readCustomer(createdCustomer.getId());
        assertNotNull(dbCustomer);
        assertEquals("John", dbCustomer.getFirstName());
    }

    @Test
    void testUpdateCustomer() throws SQLException {
        // Zunächst einen Kunden erstellen
        Customer existingCustomer = new Customer();
        existingCustomer.setId(UUID.randomUUID());
        existingCustomer.setFirstName("Jane");
        existingCustomer.setLastName("Doe");
        existingCustomer.setBirthDate(LocalDate.of(1985, 5, 15));
        existingCustomer.setGender(Gender.W);

        crudCustomer.addNewCustomer(existingCustomer);

        // Den Kunden aktualisieren
        existingCustomer.setFirstName("Janet");
        Response response = customerResource.updateCustomer(existingCustomer);

        // Überprüfen der Antwort
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Customer updated", response.getEntity());

        // Überprüfen, ob die Änderungen in der DB vorgenommen wurden
        Customer updatedCustomer = crudCustomer.readCustomer(existingCustomer.getId());
        assertNotNull(updatedCustomer);
        assertEquals("Janet", updatedCustomer.getFirstName());
    }

    @Test
    void testGetAllCustomers() throws SQLException {
        // Erstelle zwei Kunden
        Customer customer1 = new Customer(UUID.randomUUID(), "Alice", "Smith", LocalDate.of(1995, 3, 22), Gender.W);
        Customer customer2 = new Customer(UUID.randomUUID(), "Bob", "Brown", LocalDate.of(1987, 7, 15), Gender.M);
        crudCustomer.addNewCustomer(customer1);
        crudCustomer.addNewCustomer(customer2);

        // Abrufen aller Kunden (ohne User-Filter)
        Response response = customerResource.getAllCustomers(null);

        // Überprüfen der Antwort
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        // Überprüfen, ob die Liste mindestens 2 Elemente enthält
        @SuppressWarnings("unchecked")
        List<Customer> customers = (List<Customer>) response.getEntity();
        assertTrue(customers.size() >= 2);
    }

    @Test
    void testGetCustomerById() throws SQLException {
        // Einen Kunden erstellen
        Customer customer = new Customer(UUID.randomUUID(), "Charlie", "Johnson", LocalDate.of(1992, 11, 5), Gender.M);
        crudCustomer.addNewCustomer(customer);

        // Den Kunden über die Resource abrufen
        Response response = customerResource.getCustomerById(customer.getId().toString());

        // Überprüfen der Antwort
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Customer fetchedCustomer = (Customer) response.getEntity();
        assertEquals(customer.getId(), fetchedCustomer.getId());
        assertEquals("Charlie", fetchedCustomer.getFirstName());
    }

    @Test
    void testDeleteCustomer() throws SQLException {
        // Einen Kunden erstellen
        Customer customer = new Customer(UUID.randomUUID(), "Diana", "Evans", LocalDate.of(1990, 6, 10), Gender.W);
        crudCustomer.addNewCustomer(customer);

        // Den Kunden über die Resource löschen
        Response response = customerResource.deleteCustomer(customer.getId().toString());

        // Überprüfen der Antwort
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        // Sicherstellen, dass der Kunde aus der Datenbank gelöscht wurde
        Customer deletedCustomer = crudCustomer.readCustomer(customer.getId());
        assertNull(deletedCustomer);
    }

    @Test
    void testCreateCustomerWithInvalidData() throws SQLException {
        // Erstelle einen neuen Kunden mit ungültigen Daten (leerer Vorname und Nachname)
        Customer invalidCustomer = new Customer();
        invalidCustomer.setFirstName("");
        invalidCustomer.setLastName("");
        invalidCustomer.setBirthDate(LocalDate.of(1990, 1, 1));
        invalidCustomer.setGender(Gender.M);

        // Versuche, den Kunden über die Resource anzulegen
        Response response = customerResource.createCustomer(new RestCustomer(invalidCustomer));

        // Überprüfe, dass BAD_REQUEST zurückgegeben wird
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void testGetCustomerByIdNotFound() throws SQLException {
        // Verwende eine zufällige (nicht existierende) ID
        UUID nonExistentId = UUID.randomUUID();
        Response response = customerResource.getCustomerById(nonExistentId.toString());

        // Überprüfe, dass 404 zurückgegeben wird
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Customer not found", response.getEntity());
    }

    @Test
    void testCreateCustomerWithExistingId() throws SQLException {
        // Erstelle einen Kunden mit einer bestimmten ID
        UUID existingId = UUID.randomUUID();
        Customer existingCustomer = new Customer(existingId, "Alice", "Smith", LocalDate.of(1995, 3, 22), Gender.W);
        crudCustomer.addNewCustomer(existingCustomer);

        // Versuche, einen neuen Kunden mit derselben ID anzulegen
        Customer newCustomer = new Customer(existingId, "Bob", "Jones", LocalDate.of(1990, 7, 12), Gender.M);
        Response response = customerResource.createCustomer(new RestCustomer(newCustomer));

        // Überprüfe, dass ein Konflikt (409) zurückgegeben wird
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        assertEquals("Customer with this ID already exists", response.getEntity());
    }
}
