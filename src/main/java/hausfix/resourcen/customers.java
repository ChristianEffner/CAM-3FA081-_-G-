package hausfix.resourcen;

import hausfix.CRUD.CrudCustomer;
import hausfix.entities.Customer;
import hausfix.rest.RestCustomer;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Path("/customers")
public class customers {

    public customers(CrudCustomer crudCustomer) {}
    public customers() {}

    // === POST /customers => Neues Customer-Objekt anlegen
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCustomer(RestCustomer restCustomer) throws SQLException {
        var customer = restCustomer.customer();
        if (customer.getId() == null) {
            customer.setId(UUID.randomUUID());
        }

        // Falls schon existiert:
        CrudCustomer crudCustomer = new CrudCustomer();
        if (crudCustomer.readCustomer(customer.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Customer with this ID already exists")
                    .build();
        }

        // Minimale Validierung
        if (customer.getFirstName().isEmpty() || customer.getLastName().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid customer data").build();
        }

        // userId im 'customer' kann aus dem Body kommen
        // => Füge es in DB ein
        return crudCustomer.addNewCustomer(customer);
    }

    // === PUT /customers => Update
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateCustomer(Customer customer) {
        CrudCustomer customerCrud = new CrudCustomer();
        return customerCrud.updateCustomerById(customer);
    }

    // === GET /customers?userId=123 => Entweder alle oder gefiltert
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCustomers(@QueryParam("userId") Long userId) {
        CrudCustomer crud = new CrudCustomer();

        if (userId != null) {
            // Nur Customers für diesen User
            List<Customer> filtered = crud.readCustomersForUser(userId);
            return Response.ok(filtered).build();
        } else {
            // Alle
            List<Customer> all = crud.readAllCustomers();
            return Response.ok(all).build();
        }
    }

    // === GET /customers/{uuid}
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
            return Response.status(Response.Status.NOT_FOUND).entity("Customer not found").build();
        }
    }

    // === DELETE /customers/{uuid}
    @Path("/{uuid}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCustomer(@PathParam("uuid") String uuid) {
        UUID customerId = UUID.fromString(uuid);
        CrudCustomer crudCustomer = new CrudCustomer();
        Customer customer = crudCustomer.deleteCustomerById(customerId);
        return Response.ok(customer).build();
    }
}
