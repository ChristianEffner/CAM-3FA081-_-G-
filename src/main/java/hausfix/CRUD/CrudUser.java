package hausfix.CRUD;

import hausfix.Database.DatabaseConnection;
import hausfix.entities.User;
import hausfix.security.PasswordUtil;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * REST-CRUD-Ressource für Benutzer.
 * <p>
 * Endpunkte:
 *   GET    /users            – Liste aller Benutzer (ohne Passwort-Hash)
 *   GET    /users/{id}       – Einzelner Benutzer (ohne Passwort-Hash)
 *   POST   /users            – Benutzer anlegen (Passwort wird sofort gehasht)
 *   PUT    /users/{id}       – Benutzer aktualisieren (optional neues Passwort)
 *   DELETE /users/{id}       – Benutzer löschen
 *   POST   /users/login      – Login (Passwort-Hash-Verifizierung)
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CrudUser {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    /* -------------------------------------------------- READ -------------------------------------------------- */

    @GET
    public Response getAllUsers() {
        if (db.connection == null)
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"error\":\"DB-Verbindung nicht verfügbar\"}").build();

        String sql = "SELECT id, username FROM `users`";
        List<User> users = new ArrayList<>();

        try (var st = db.connection.createStatement();
             var rs = st.executeQuery(sql)) {

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(null);          // Passwort niemals ausgeben
                users.add(u);
            }
            return Response.ok(users).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") long id) {
        if (db.connection == null)
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"error\":\"DB-Verbindung nicht verfügbar\"}").build();

        String sql = "SELECT id, username FROM `users` WHERE id = ?";
        try (var ps = db.connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getLong("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPassword(null);
                    return Response.ok(u).build();
                }
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Benutzer nicht gefunden\"}").build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    /* -------------------------------------------------- CREATE -------------------------------------------------- */

    @POST
    public Response createUser(User user) {
        if (db.connection == null)
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"error\":\"DB-Verbindung nicht verfügbar\"}").build();

        String sql = "INSERT INTO `users` (username, password) VALUES (?, ?)";
        try (var ps = db.connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordUtil.hash(user.getPassword()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getLong(1));
            }
            user.setPassword(null);
            return Response.status(Response.Status.CREATED).entity(user).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    /* -------------------------------------------------- UPDATE -------------------------------------------------- */

    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") long id, User updated) {
        if (db.connection == null)
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"error\":\"DB-Verbindung nicht verfügbar\"}").build();

        boolean pwProvided = updated.getPassword() != null && !updated.getPassword().isBlank();
        String sql = pwProvided
                ? "UPDATE `users` SET username = ?, password = ? WHERE id = ?"
                : "UPDATE `users` SET username = ?          WHERE id = ?";

        try (var ps = db.connection.prepareStatement(sql)) {
            ps.setString(1, updated.getUsername());

            if (pwProvided) {
                ps.setString(2, PasswordUtil.hash(updated.getPassword()));
                ps.setLong(3, id);
            } else {
                ps.setLong(2, id);
            }

            if (ps.executeUpdate() == 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Benutzer nicht gefunden\"}").build();
            }
            return Response.ok("{\"message\":\"Benutzer aktualisiert\"}").build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    /* -------------------------------------------------- DELETE -------------------------------------------------- */

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") long id) {
        if (db.connection == null)
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"error\":\"DB-Verbindung nicht verfügbar\"}").build();

        String sql = "DELETE FROM `users` WHERE id = ?";
        try (var ps = db.connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            if (ps.executeUpdate() == 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Benutzer nicht gefunden\"}").build();
            }
            return Response.ok("{\"message\":\"Benutzer gelöscht\"}").build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    /* -------------------------------------------------- LOGIN -------------------------------------------------- */

    @POST
    @Path("/login")
    public Response login(User credentials) {
        if (db.connection == null)
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"error\":\"DB-Verbindung nicht verfügbar\"}").build();

        String sql = "SELECT id, username, password FROM `users` WHERE username = ? LIMIT 1";
        try (var ps = db.connection.prepareStatement(sql)) {
            ps.setString(1, credentials.getUsername());

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (PasswordUtil.verify(credentials.getPassword(), storedHash)) {
                        User u = new User();
                        u.setId(rs.getLong("id"));
                        u.setUsername(rs.getString("username"));
                        return Response.ok(u).build();
                    }
                }
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Login fehlgeschlagen\"}").build();
            }

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }
}
