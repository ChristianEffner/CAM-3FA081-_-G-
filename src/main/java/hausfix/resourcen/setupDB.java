package hausfix.resourcen;
import hausfix.Database.DatabaseConnection;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/setupDB")
public class setupDB {

    @DELETE
    public Response resetDatabase() {
        // Logik zum Löschen und Neuanlegen der Tabellen Kunde und Ablesung
        DatabaseConnection databaseConnection = new DatabaseConnection();
        databaseConnection.removeAllTables();
        databaseConnection.createAllTables();
        return Response.ok("Database reset successful").build();
    }
}

