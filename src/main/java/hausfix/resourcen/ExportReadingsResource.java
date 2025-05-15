package hausfix.resourcen;
import hausfix.CRUD.CrudReading;
import hausfix.entities.Customer;
import hausfix.entities.Reading;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.StringWriter;
import java.util.List;

@Path("/export")
public class ExportReadingsResource {

    private final CrudReading crudReading = new CrudReading();

    @GET
    @Path("/readings")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "text/csv" })
    public Response exportReadings(@QueryParam("format") String format,
                                   @QueryParam("userId") Long userId) {
        List<Reading> readings = (userId != null)
                ? crudReading.readAllReadingForUser(userId)
                : crudReading.readAllReading();

        readings.removeIf(r -> r.getCustomer() == null || r.getCustomer().getId() == null);

        if (readings.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT)
                    .entity("Keine Ablesedaten gefunden")
                    .build();
        }

        switch (format.toLowerCase()) {
            case "json":
                return Response.ok(readings, MediaType.APPLICATION_JSON)
                        .header("Content-Disposition", "attachment; filename=\"readings.json\"")
                        .build();

            case "xml":
                try {
                    ReadingList wrapper = new ReadingList(readings);
                    JAXBContext ctx = JAXBContext.newInstance(ReadingList.class, Reading.class, Customer.class);
                    StringWriter sw = new StringWriter();
                    ctx.createMarshaller().marshal(wrapper, sw);
                    System.out.println(sw.toString());
                } catch (JAXBException e) {
                    e.printStackTrace();
                    return Response.serverError()
                            .entity("Fehler beim XML-Export: " + e.getMessage())
                            .build();
                }
                return Response.ok(new ReadingList(readings), MediaType.APPLICATION_XML)
                        .header("Content-Disposition", "attachment; filename=\"readings.xml\"")
                        .build();

            case "csv":
                return Response.ok(toCsv(readings), "text/csv")
                        .header("Content-Disposition", "attachment; filename=\"readings.csv\"")
                        .build();

            default:
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Ungültiges Format")
                        .build();
        }
    }



    // Hilfsmethode zum CSV-Export
    private String toCsv(List<Reading> readings) {
        StringBuilder sb = new StringBuilder("Reading ID,Customer ID,Date,Value,KindOfMeter\n");
        for (Reading r : readings) {
            sb.append(r.getId()).append(",")
                    .append(r.getCustomer().getId()).append(",")
                    .append(r.getDateOfReading()).append(",")
                    .append(r.getMeterCount()).append(",")
                    .append(r.getKindOfMeter()).append("\n");
        }
        return sb.toString();
    }
}
