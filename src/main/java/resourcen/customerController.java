package resourcen;
import hausfix.entities.Customer;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import hausfix.CRUD.CrudCustomer;

import java.util.UUID;

@Path("/customers")
public class customerController {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCustomer(Customer customer) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Unable to create customer: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
















