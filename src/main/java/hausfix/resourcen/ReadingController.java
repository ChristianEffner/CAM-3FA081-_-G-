package hausfix.resourcen;

import com.fasterxml.jackson.databind.ObjectMapper;
import hausfix.CRUD.CrudCustomer;
import hausfix.CRUD.CrudReading;
import hausfix.entities.Customer;
import hausfix.entities.Reading;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/readings")
public class ReadingController {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCustomer(Reading reading) {
        try {
            // UUID generieren, falls nicht vorhanden
            if (reading.getId() == null) {
                reading.setId(UUID.randomUUID());
            }

            // Neuen Kunden hinzufügen
            CrudReading reading1 = new CrudReading();
            reading1.addNewReading(reading);

            // Antwort
            return Response.status(Response.Status.CREATED)
                    .entity(reading) // Den gespeicherten Kunden mit UUID zurückgeben
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Unable to create reading: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
