package hausfix.resourcen;
import hausfix.entities.Customer;
import hausfix.rest.RestCustomer;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import hausfix.CRUD.CrudCustomer;

import java.sql.SQLException;
import java.util.UUID;

@Path("/customers")
public class customerController {

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
    public Response updateCustomer(RestCustomer restCustomer) {
        // Validierung der Eingabedaten
        if (restCustomer == null || restCustomer.customer() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid customer data provided.")
                    .build();
        }

        // Kundendaten aktualisieren
        CrudCustomer crudCustomer = new CrudCustomer();

        // Aufruf der Methode
        crudCustomer.updateCustomerById(restCustomer.customer());

        // Erfolgsantwort (keine Ausnahme ausgelöst)
        return Response.status(Response.Status.OK)
                .entity("Customer successfully updated.")
                .build();

    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomer(RestCustomer restCustomer) {

        if (restCustomer == null || restCustomer.customer() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No Customer found.")
                    .build();
        }

        CrudCustomer crudCustomer = new CrudCustomer();
        UUID customerId = UUID.fromString("87783eb7-956e-4a4e-b4c6-32123a703e87");  //Wert aus Datenbank genommen
        crudCustomer.readCustomer(customerId);

        return Response.status(Response.Status.OK)
                .entity("get Customer succeded.")
                .build();

    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCustomer(RestCustomer restCustomer) {

        if (restCustomer == null || restCustomer.customer() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No Customer found.")
                    .build();
        }

        CrudCustomer crudCustomer = new CrudCustomer();
        UUID customerId = UUID.fromString("87783eb7-956e-4a4e-b4c6-32123a703e87");  //Wert aus Datenbank genommen
        crudCustomer.deleteCustomerById(customerId);

        return Response.status(Response.Status.OK)
                .entity("Customer deleted succesfully.")
                .build();
    }

}
















