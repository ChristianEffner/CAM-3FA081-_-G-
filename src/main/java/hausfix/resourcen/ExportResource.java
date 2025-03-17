package hausfix.resourcen;

import hausfix.CRUD.CrudCustomer;
import hausfix.entities.Customer;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/export")
public class ExportResource {

    private final CrudCustomer crudCustomer = new CrudCustomer();

    @GET
    @Path("/customers")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "text/csv"})
    public Response exportCustomers(@QueryParam("format") String format) {
        List<Customer> customers = crudCustomer.readAllCustomers();

        if (customers.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).entity("Keine Kunden gefunden").build();
        }

        switch (format.toLowerCase()) {
            case "json":
                return Response.ok(customers, MediaType.APPLICATION_JSON).build();

            case "xml":
                return Response.ok(customers, MediaType.APPLICATION_XML).build();

            case "csv":
                return Response.ok(convertToCSV(customers))
                        .type("text/csv")
                        .header("Content-Disposition", "attachment; filename=\"customers.csv\"")
                        .build();

            default:
                return Response.status(Response.Status.BAD_REQUEST).entity("Ungültiges Format").build();
        }
    }


    private String convertToCSV(List<Customer> customers) {
        StringBuilder csvData = new StringBuilder("ID,First Name,Last Name,Birth Date,Gender,User ID\n");
        for (Customer customer : customers) {
            csvData.append(customer.getId()).append(",");
            csvData.append(customer.getFirstName()).append(",");
            csvData.append(customer.getLastName()).append(",");
            csvData.append(customer.getBirthDate()).append(",");
            csvData.append(customer.getGender()).append(",");
            csvData.append(customer.getUserId() != null ? customer.getUserId() : "").append("\n");
        }
        return csvData.toString();
    }
}



