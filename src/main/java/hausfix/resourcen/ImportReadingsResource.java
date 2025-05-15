package hausfix.resourcen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hausfix.CRUD.CrudReading;
import hausfix.entities.Customer;
import hausfix.entities.Reading;
import hausfix.enums.KindOfMeter;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Path("/import")
public class ImportReadingsResource {

    private final CrudReading crudReading = new CrudReading();

    @POST
    @Path("/readings")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importReadings(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @QueryParam("format") String format) {

        if (fileInputStream == null || fileDetail == null || format == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Fehlende Datei oder Format\"}")
                    .build();
        }

        try {
            List<Reading> readings;
            switch (format.toLowerCase()) {
                case "csv":
                    readings = parseCSV(fileInputStream);
                    break;
                case "json":
                    readings = parseJSON(fileInputStream);
                    break;
                case "xml":
                    readings = parseXML(fileInputStream);
                    break;
                default:
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"message\":\"Ungültiges Format\"}")
                            .build();
            }

            if (readings.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\":\"Keine Ablesedaten gefunden.\"}")
                        .build();
            }

            for (Reading reading : readings) {
                if (reading.getCustomer() == null || reading.getCustomer().getId() == null) {
                    System.out.println("⚠️ Reading ohne Customer-ID wird ignoriert.");
                    continue;
                }

                reading.setId(UUID.randomUUID());
                if (reading.getSubstitute() == null) reading.setSubstitute(false);
                crudReading.addNewReading(reading);
            }

            return Response.ok("{\"message\":\"Import erfolgreich!\"}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\":\"Fehler beim Import: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    private List<Reading> parseCSV(InputStream input) throws IOException {
        List<Reading> readings = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build())) {

            for (CSVRecord record : parser) {
                String custIdStr = record.get("Customer ID");
                if (custIdStr == null || custIdStr.isBlank()) {
                    System.out.println("⚠️ Überspringe Zeile ohne Customer-ID");
                    continue;
                }

                Customer customer = new Customer();
                customer.setId(UUID.fromString(custIdStr));

                Reading reading = new Reading();
                reading.setCustomer(customer);
                reading.setDateOfReading(LocalDate.parse(record.get("Date")));
                reading.setMeterCount(Double.parseDouble(record.get("Value")));
                reading.setKindOfMeter(KindOfMeter.valueOf(record.get("KindOfMeter")));
                reading.setMeterId(record.isMapped("MeterId") ? record.get("MeterId") : "");
                reading.setComment(record.isMapped("Comment") ? record.get("Comment") : "");
                reading.setSubstitute(false);

                readings.add(reading);
            }
        }
        return readings;
    }

    private List<Reading> parseJSON(InputStream input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        List<Reading> readings = Arrays.asList(mapper.readValue(input, Reading[].class));

        // Filter ungültige Kunden raus
        readings.removeIf(r -> r.getCustomer() == null || r.getCustomer().getId() == null);
        return readings;
    }

    private List<Reading> parseXML(InputStream input) throws JAXBException {
        ReadingList wrapper = (ReadingList) JAXBContext
                .newInstance(ReadingList.class)
                .createUnmarshaller()
                .unmarshal(new InputStreamReader(input, StandardCharsets.UTF_8));
        return wrapper.getReadings();
    }
}
