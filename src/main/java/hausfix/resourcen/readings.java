package hausfix.resourcen;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllReadings(
            @QueryParam("customer") UUID customer,
            @QueryParam("start") String startDate,
            @QueryParam("end") String endDate,
            @QueryParam("kindOfMeter") String kindOfMeterString) {

        CrudReading readingCrud = new CrudReading();

        // Standardwerte setzen, falls keine Daten angegeben sind
        LocalDate start = (startDate != null) ? LocalDate.parse(startDate) : LocalDate.MIN;
        LocalDate end = (endDate != null) ? LocalDate.parse(endDate) : LocalDate.now();

        KindOfMeter kindOfMeter = null;
        if (kindOfMeterString != null) {
            try {
                kindOfMeter = KindOfMeter.valueOf(kindOfMeterString.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid kindOfMeter value. Please provide a valid value.")
                        .build();
            }
        }

        // Validierung: Startdatum darf nicht nach dem Enddatum liegen
        if (start.isAfter(end)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Start date cannot be after end date.")
                    .build();
        }

        // Readings abrufen und nach den Filtern filtern
        List<Reading> allReadings = readingCrud.readAllReading();
        KindOfMeter finalKindOfMeter = kindOfMeter;
        List<Reading> filteredReadings = allReadings.stream()
                .filter(r -> (customer == null || r.getCustomer().getId().equals(customer)))
                .filter(r -> !r.getDateOfReading().isBefore(start) && !r.getDateOfReading().isAfter(end))
                .filter(r -> (finalKindOfMeter == null || r.getKindOfMeter().equals(finalKindOfMeter)))
                .collect(Collectors.toList());

        return Response.ok(filteredReadings).build();
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

}

