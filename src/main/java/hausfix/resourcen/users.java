package hausfix.resourcen;

import hausfix.CRUD.CrudUser;
import hausfix.entities.User;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * JAX-RS Ressource für "users" - analog zu "customers" oder "readings".
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class users {

    private CrudUser crudUser = new CrudUser();

    // === GET /users => alle Benutzer abfragen
    @GET
    public Response getAllUsers() {
        return crudUser.getAllUsers();
    }

    // === GET /users/{id} => Einzelnen Benutzer abfragen
    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Long id) {
        return crudUser.getUserById(id);
    }

    // === POST /users => Neuen Benutzer anlegen
    @POST
    public Response createUser(User user) {
        return crudUser.createUser(user);
    }

    // === PUT /users/{id} => Benutzer aktualisieren
    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") Long id, User updatedUser) {
        return crudUser.updateUser(id, updatedUser);
    }

    // === DELETE /users/{id} => Benutzer löschen
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") Long id) {
        return crudUser.deleteUser(id);
    }

    // === POST /users/login => Login-Endpunkt
    @POST
    @Path("/login")
    public Response login(User user) {
        return crudUser.login(user);
    }
}
