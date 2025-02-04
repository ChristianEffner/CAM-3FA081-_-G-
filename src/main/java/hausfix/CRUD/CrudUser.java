package hausfix.CRUD;

import hausfix.Database.DatabaseConnection;
import hausfix.entities.User;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CrudUser {

    private final DatabaseConnection databaseConnection = DatabaseConnection.getInstance();

    @GET
    public Response getAllUsers() {
        try {
            String query = "SELECT * FROM `users`"; // Klein
            List<User> users = new ArrayList<>();

            try (var statement = databaseConnection.connection.createStatement();
                 var resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getLong("id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setPassword(resultSet.getString("password"));
                    users.add(user);
                }
            }
            return Response.ok(users).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Abrufen der Benutzer: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Long id) {
        try {
            String query = "SELECT * FROM `users` WHERE id = ?";
            User user = null;

            try (var preparedStatement = databaseConnection.connection.prepareStatement(query)) {
                preparedStatement.setLong(1, id);
                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        user = new User();
                        user.setId(resultSet.getLong("id"));
                        user.setUsername(resultSet.getString("username"));
                        user.setPassword(resultSet.getString("password"));
                    }
                }
            }

            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Benutzer nicht gefunden").build();
            }
            return Response.ok(user).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Abrufen des Benutzers: " + e.getMessage()).build();
        }
    }

    @POST
    public Response createUser(User user) {
        try {
            // Insert in kleingeschriebene Tabelle
            String query = "INSERT INTO `users` (username, password) VALUES (?, ?)";

            try (var preparedStatement = databaseConnection.connection.prepareStatement(query)) {
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getPassword());
                preparedStatement.executeUpdate();
            }

            return Response.status(Response.Status.CREATED)
                    .entity("Benutzer erfolgreich erstellt").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Erstellen des Benutzers: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") Long id, User updatedUser) {
        try {
            String query = "UPDATE `users` SET username = ?, password = ? WHERE id = ?";

            try (var preparedStatement = databaseConnection.connection.prepareStatement(query)) {
                preparedStatement.setString(1, updatedUser.getUsername());
                preparedStatement.setString(2, updatedUser.getPassword());
                preparedStatement.setLong(3, id);
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected == 0) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Benutzer nicht gefunden").build();
                }
            }

            return Response.ok("Benutzer erfolgreich aktualisiert").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Aktualisieren des Benutzers: " + e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") Long id) {
        try {
            String query = "DELETE FROM `users` WHERE id = ?";

            try (var preparedStatement = databaseConnection.connection.prepareStatement(query)) {
                preparedStatement.setLong(1, id);
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected == 0) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Benutzer nicht gefunden").build();
                }
            }

            return Response.ok("Benutzer erfolgreich gelöscht").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Löschen des Benutzers: " + e.getMessage()).build();
        }
    }

    /**
     * Neuer Endpoint für Login:
     * Wir empfangen ein JSON-Objekt {"username":"...","password":"..."}
     * und prüfen, ob der Nutzer existiert und das Passwort passt.
     * Gibt 200 OK bei Erfolg, 401 Unauthorized sonst.
     */
    @POST
    @Path("/login")
    public Response login(User user) {
        try {
            String username = user.getUsername();
            String password = user.getPassword();

            // Ganz simple Prüfung: SELECT * FROM `users` WHERE username=? AND password=?
            // (In der Realität: Passwörter gehasht + salt)
            String sql = "SELECT * FROM `users` WHERE username = ? AND password = ? LIMIT 1";

            try (var preparedStatement = databaseConnection.connection.prepareStatement(sql)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);

                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        // Erfolg => 200 OK
                        User foundUser = new User();
                        foundUser.setId(rs.getLong("id"));
                        foundUser.setUsername(rs.getString("username"));
                        // foundUser.setPassword(rs.getString("password")); // i.d.R. nicht zurückgeben

                        return Response.ok(foundUser).build();
                    } else {
                        // Nichts gefunden => 401 Unauthorized
                        return Response.status(Response.Status.UNAUTHORIZED)
                                .entity("Login fehlgeschlagen: Benutzername oder Passwort inkorrekt.")
                                .build();
                    }
                }
            }

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Login: " + e.getMessage()).build();
        }
    }
}
