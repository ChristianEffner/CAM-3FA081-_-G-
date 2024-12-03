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
}
















