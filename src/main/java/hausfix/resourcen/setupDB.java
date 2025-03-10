package hausfix.resourcen;

import hausfix.Database.DatabaseConnection;
import hausfix.entities.User;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/setupDB")
public class setupDB {

    @DELETE
    public Response resetDatabase() {
        // Logik zum Löschen und Neuanlegen der Tabellen
        DatabaseConnection databaseConnection = DatabaseConnection.getInstance();
        databaseConnection.removeAllTables();
        databaseConnection.createAllTables();
        return Response.ok("Datenbank wurde erfolgreich zurückgesetzt").build();
    }

    @POST
    @Path("/initAdmin")
    public Response initAdminUser() {
        try {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

            // Admin-Benutzer erstellen
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin");

            databaseConnection.save(admin);
            return Response.status(Response.Status.CREATED).entity("Admin-Benutzer wurde erfolgreich erstellt").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Erstellen des Admin-Benutzers: " + e.getMessage())
                    .build();
        }
    }
}