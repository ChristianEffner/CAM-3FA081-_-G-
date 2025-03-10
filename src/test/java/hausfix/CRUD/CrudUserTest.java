package hausfix.CRUD;

import hausfix.Database.DatabaseConnection;
import hausfix.entities.User;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static hausfix.Main.getProperties;
import static org.junit.jupiter.api.Assertions.*;

class CrudUserTest {

    private static CrudUser crudUser;
    private static Connection connection;

    @BeforeAll
    public static void setUp() throws SQLException {
        DatabaseConnection dbManager = DatabaseConnection.getInstance();
        connection = dbManager.openConnection(getProperties());
        crudUser = new CrudUser();
        // Tabelle users leeren
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM `users`");
            connection.commit();
        }
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        DatabaseConnection dbManager = DatabaseConnection.getInstance();
        // Optional: Tabelle zurücksetzen
        connection = dbManager.openConnection(getProperties());
        dbManager.truncateAllTables();
        dbManager.closeConnection();
    }

    @Test
    public void testCreateUserSuccess() throws SQLException {
        User newUser = new User("testuser", "password123");
        Response response = crudUser.createUser(newUser);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        User createdUser = (User) response.getEntity();
        assertNotNull(createdUser.getId(), "User ID should be generated.");
        assertEquals("testuser", createdUser.getUsername());
        // Aus Sicherheitsgründen wird das Passwort in der Response auf null gesetzt
        assertNull(createdUser.getPassword());

        // Überprüfe, ob der Benutzer in der DB vorhanden ist
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `users` WHERE id = ?")) {
            stmt.setLong(1, createdUser.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "User should exist in the database.");
                assertEquals("testuser", rs.getString("username"));
            }
        }
    }

    @Test
    public void testGetAllUsers() throws SQLException {
        // Zwei Benutzer anlegen
        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");
        crudUser.createUser(user1);
        crudUser.createUser(user2);

        Response response = crudUser.getAllUsers();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) response.getEntity();
        assertTrue(users.size() >= 2, "At least two users should be returned.");
    }

    @Test
    public void testGetUserByIdSuccess() {
        User newUser = new User("user_get", "pass_get");
        Response createResponse = crudUser.createUser(newUser);
        User createdUser = (User) createResponse.getEntity();
        Long userId = createdUser.getId();

        Response response = crudUser.getUserById(userId);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        User retrievedUser = (User) response.getEntity();
        assertEquals(userId, retrievedUser.getId());
        assertEquals("user_get", retrievedUser.getUsername());
        // Bei getUserById wird das Passwort zurückgegeben
        assertEquals("pass_get", retrievedUser.getPassword());
    }

    @Test
    public void testGetUserByIdNotFound() {
        Response response = crudUser.getUserById(999999L);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        String msg = (String) response.getEntity();
        assertTrue(msg.contains("Benutzer nicht gefunden"));
    }

    @Test
    public void testUpdateUserSuccess() {
        User newUser = new User("updateTest", "initialPass");
        Response createResponse = crudUser.createUser(newUser);
        User createdUser = (User) createResponse.getEntity();
        Long userId = createdUser.getId();

        User updatedUser = new User("updatedUser", "newPass");
        Response updateResponse = crudUser.updateUser(userId, updatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), updateResponse.getStatus());
        String updateMsg = (String) updateResponse.getEntity();
        assertTrue(updateMsg.contains("Benutzer erfolgreich aktualisiert"));

        Response getResponse = crudUser.getUserById(userId);
        User retrievedUser = (User) getResponse.getEntity();
        assertEquals("updatedUser", retrievedUser.getUsername());
        assertEquals("newPass", retrievedUser.getPassword());
    }

    @Test
    public void testUpdateUserNotFound() {
        User updatedUser = new User("nonExistent", "nopass");
        Response response = crudUser.updateUser(888888L, updatedUser);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        String msg = (String) response.getEntity();
        assertTrue(msg.contains("Benutzer nicht gefunden"));
    }

    @Test
    public void testDeleteUserSuccess() {
        User newUser = new User("deleteTest", "deletePass");
        Response createResponse = crudUser.createUser(newUser);
        User createdUser = (User) createResponse.getEntity();
        Long userId = createdUser.getId();

        Response deleteResponse = crudUser.deleteUser(userId);
        assertEquals(Response.Status.OK.getStatusCode(), deleteResponse.getStatus());
        String deleteMsg = (String) deleteResponse.getEntity();
        assertTrue(deleteMsg.contains("Benutzer erfolgreich gelöscht"));

        Response getResponse = crudUser.getUserById(userId);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());
    }

    @Test
    public void testDeleteUserNotFound() {
        Response response = crudUser.deleteUser(777777L);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        String msg = (String) response.getEntity();
        assertTrue(msg.contains("Benutzer nicht gefunden"));
    }

    @Test
    public void testLoginSuccess() {
        // Erstelle einen Benutzer, der sich einloggen kann
        User newUser = new User("loginUser", "loginPass");
        Response createResponse = crudUser.createUser(newUser);
        User createdUser = (User) createResponse.getEntity();
        Long userId = createdUser.getId();

        User loginAttempt = new User("loginUser", "loginPass");
        Response loginResponse = crudUser.login(loginAttempt);
        assertEquals(Response.Status.OK.getStatusCode(), loginResponse.getStatus());
        User loggedInUser = (User) loginResponse.getEntity();
        assertEquals(userId, loggedInUser.getId());
        assertEquals("loginUser", loggedInUser.getUsername());
        // Passwort wird aus Sicherheitsgründen nicht zurückgegeben
        assertNull(loggedInUser.getPassword());
    }

    @Test
    public void testLoginFailure() {
        User newUser = new User("loginFailUser", "correctPass");
        crudUser.createUser(newUser);

        User loginAttempt = new User("loginFailUser", "wrongPass");
        Response loginResponse = crudUser.login(loginAttempt);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), loginResponse.getStatus());
        String msg = (String) loginResponse.getEntity();
        assertTrue(msg.contains("Login fehlgeschlagen"));
    }
}
