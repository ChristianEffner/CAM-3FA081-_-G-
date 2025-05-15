package hausfix.resourcen;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hausfix.CRUD.CrudCustomer;
import hausfix.entities.Customer;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Path("/import")
public class ImportResource {

    private final CrudCustomer crudCustomer = new CrudCustomer();

    @POST
    @Path("/customers")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importCustomers(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @QueryParam("format") String format) {

        if (fileInputStream == null || format == null || fileDetail == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Fehlende Datei oder Format").build();
        }

        try {
            List<Customer> customers = new ArrayList<>();

            if ("csv".equalsIgnoreCase(format)) {
                customers = parseCSV(fileInputStream);
            } else if ("json".equalsIgnoreCase(format)) {
                customers = parseJSON(fileInputStream);
            } else if ("xml".equalsIgnoreCase(format)) {
                customers = parseXML(fileInputStream);
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("Ungültiges Importformat").build();
            }

            if (customers.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Kein Kunde vorhanden, füge einen Kunden hinzu.\"}")
                        .build();
            }

            for (Customer customer : customers) {
                customer.setId(UUID.randomUUID()); // Generiere eine neue UUID
                crudCustomer.addNewCustomer(customer);
            }

            return Response.ok("{\"message\": \"Import erfolgreich!\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Fehler beim Import: " + e.getMessage() + "\"}")
                    .build();
        }
    }


    private List<Customer> parseCSV(InputStream fileInputStream) throws IOException {
        List<Customer> customers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                Customer customer = new Customer();
                customer.setFirstName(record.get("First Name"));
                customer.setLastName(record.get("Last Name"));
                customer.setBirthDate(java.time.LocalDate.parse(record.get("Birth Date")));
                customer.setGender(hausfix.enums.Gender.valueOf(record.get("Gender")));
                customer.setUserId(Long.parseLong(record.get("User ID")));

                customers.add(customer);
            }
        }
        return customers;
    }

    private List<Customer> parseJSON(InputStream fileInputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        return new ArrayList<>(Arrays.asList(
                mapper.readValue(fileInputStream, Customer[].class)
        ));
    }

    private List<Customer> parseXML(InputStream fileInputStream) throws JAXBException {
        CustomerList wrapper = (CustomerList) JAXBContext
                .newInstance(CustomerList.class)
                .createUnmarshaller()
                .unmarshal(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8));

        return wrapper.getCustomers();
    }
}
