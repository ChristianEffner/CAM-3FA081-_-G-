package hausfix.resourcen;
import hausfix.entities.Customer;
import hausfix.rest.RestCustomer;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import hausfix.CRUD.CrudCustomer;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Path("/customers")
public class customers {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCustomer(RestCustomer restCustomer) throws SQLException {
        var customer = restCustomer.customer();
        // UUID generieren, falls nicht vorhanden
        if (customer.getId() == null) {
            customer.setId(UUID.randomUUID());
        }

        // Neuen Kunden hinzufügen
        CrudCustomer customer1 = new CrudCustomer();
        customer1.addNewCustomer(customer);

        // Antwort
        return Response.status(Response.Status.CREATED)
                .entity(customer) // Den gespeicherten Kunden mit UUID zurückgeben
                .build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateCustomer(Customer customer) {

        // Kunden erstellen
        CrudCustomer customer1 = new CrudCustomer();
        customer1.updateCustomerById(customer);
        // Logik zur Aktualisierung eines Kunden
        return Response.ok("Customer updated").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCustomers() {
        // Logik zum Abrufen aller Kunden
        CrudCustomer customer = new CrudCustomer();
        List<Customer> customers = customer.readAllCustomers();
        return Response.ok(customers).build();
    }

    @Path("/{uuid}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomerById(@PathParam("uuid") String uuid) {
        UUID customerId = UUID.fromString(uuid);
        CrudCustomer crudCustomer = new CrudCustomer();
        Customer customer = crudCustomer.readCustomer(customerId);

        if (customer != null) {
            System.out.println("Customer found: " + customer.getFirstName() + " " + customer.getLastName());
            return Response.ok(customer).build();
        } else {
            System.out.println("Customer not found.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{uuid}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCustomer(@PathParam("uuid") String uuid) {
        // Logik zum Löschen eines Kunden
        UUID customerId = UUID.fromString(uuid);
        CrudCustomer crudCustomer = new CrudCustomer();
        Customer customer = crudCustomer.deleteCustomerById(customerId);
        return Response.ok(customer).build();
    }
}

