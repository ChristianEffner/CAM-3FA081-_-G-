package hausfix.resourcen;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import hausfix.CRUD.CrudCustomer;
import hausfix.CRUD.CrudReading;
import hausfix.entities.Customer;
import hausfix.entities.Reading;
import hausfix.enums.KindOfMeter;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/readings")
public class readings {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createReading(Reading reading) {

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
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateReading(Reading reading) {
        // Logik zur Aktualisierung eines Ablesungsobjekts
        CrudReading crudReading = new CrudReading();
        crudReading.updateReadingById(reading);
        return Response.ok("Reading updated").build();
    }

    @DELETE
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteReading(@PathParam("uuid") String uuid) {
        // Logik zum Löschen eines Ablesungsobjekts
        UUID readingId = UUID.fromString(uuid);
        CrudReading crudReading = new CrudReading();
        Reading reading = crudReading.deleteReadingById(readingId);
        return Response.ok(reading).build();
    }

    @Path("/{uuid}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadingById(@PathParam("uuid") String uuid) {
        UUID readingId = UUID.fromString(uuid);
        CrudReading crudReading = new CrudReading();
        Reading reading = crudReading.readReading(readingId);

        if (reading != null) {
            System.out.println("Reading found: " + reading.getId() + " " + reading.getCustomer());
            return Response.ok(reading).build();
        } else {
            System.out.println("Customer not found.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/readings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings(
            @QueryParam("customer") UUID id,
            @QueryParam("start") LocalDate startDate,
            @QueryParam("end") LocalDate endDate,
            @QueryParam("kindOfMeter") KindOfMeter kindOfMeter) {

        try {
            List<Reading> readings = CrudReading.getReadings(id, startDate, endDate, kindOfMeter);
            Reading reading = new Reading();
            return Response.ok(reading).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

}

