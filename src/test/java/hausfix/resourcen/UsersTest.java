package hausfix.resourcen;

import hausfix.Database.DatabaseConnection;
import hausfix.entities.User;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;

import static hausfix.Main.getProperties;
import static org.junit.jupiter.api.Assertions.*;

class UsersTest {

    private static Connection connection;
    private static Users usersResource;

    @BeforeAll
    public static void setUp() throws SQLException {
        DatabaseConnection dbManager = DatabaseConnection.getInstance();
        connection = dbManager.openConnection(getProperties());
        // Tabelle users leeren
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM `users`");
            connection.commit();
        }
        usersResource = new Users();
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        DatabaseConnection dbManager = DatabaseConnection.getInstance();
        connection = dbManager.openConnection(getProperties());
        dbManager.truncateAllTables();
        dbManager.closeConnection();
    }

    @Test
    public void testCreateUserEndpoint() {
        User newUser = new User("resourceUser", "resPass");
        Response response = usersResource.createUser(newUser);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        User createdUser = (User) response.getEntity();
        assertNotNull(createdUser.getId());
        assertEquals("resourceUser", createdUser.getUsername());
        // Passwort wird in der Response aus Sicherheitsgründen entfernt
        assertNull(createdUser.getPassword());
    }

    @Test
    public void testGetAllUsersEndpoint() {
        // Zwei Benutzer anlegen
        User user1 = new User("userA", "passA");
        User user2 = new User("userB", "passB");
        usersResource.createUser(user1);
        usersResource.createUser(user2);

        Response response = usersResource.getAllUsers();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) response.getEntity();
        assertTrue(users.size() >= 2, "At least two users should be returned.");
    }

    @Test
    public void testGetUserByIdEndpoint() {
        User newUser = new User("getResourceUser", "passGet");
        Response createResponse = usersResource.createUser(newUser);
        User createdUser = (User) createResponse.getEntity();
        Long id = createdUser.getId();

        Response response = usersResource.getUserById(id);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        User retrievedUser = (User) response.getEntity();
        assertEquals("getResourceUser", retrievedUser.getUsername());
    }

    @Test
    public void testGetUserByIdNotFoundEndpoint() {
        Response response = usersResource.getUserById(123456789L);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testUpdateUserEndpoint() {
        User newUser = new User("updateResUser", "oldPass");
        Response createResponse = usersResource.createUser(newUser);
        User createdUser = (User) createResponse.getEntity();
        Long id = createdUser.getId();

        User updatedUser = new User("updatedResUser", "newPass");
        Response updateResponse = usersResource.updateUser(id, updatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), updateResponse.getStatus());
        String msg = (String) updateResponse.getEntity();
        assertTrue(msg.contains("Benutzer aktualisiert"));

        Response getResponse = usersResource.getUserById(id);
        User retrievedUser = (User) getResponse.getEntity();
        assertEquals("updatedResUser", retrievedUser.getUsername());
        // Bei getUserById wird das Passwort wie in CrudUser zurückgegeben
    }

    @Test
    public void testDeleteUserEndpoint() {
        User newUser = new User("deleteResUser", "deletePass");
        Response createResponse = usersResource.createUser(newUser);
        User createdUser = (User) createResponse.getEntity();
        Long id = createdUser.getId();

        Response deleteResponse = usersResource.deleteUser(id);
        assertEquals(Response.Status.OK.getStatusCode(), deleteResponse.getStatus());
        String msg = (String) deleteResponse.getEntity();
        assertTrue(msg.contains("Benutzer gelöscht"));

        Response getResponse = usersResource.getUserById(id);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());
    }

    @Test
    public void testDeleteUserNotFoundEndpoint() {
        Response response = usersResource.deleteUser(987654321L);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testLoginEndpointSuccess() {
        User newUser = new User("loginResUser", "loginResPass");
        Response createResponse = usersResource.createUser(newUser);
        User createdUser = (User) createResponse.getEntity();
        Long id = createdUser.getId();

        User loginAttempt = new User("loginResUser", "loginResPass");
        Response loginResponse = usersResource.login(loginAttempt);
        assertEquals(Response.Status.OK.getStatusCode(), loginResponse.getStatus());
        User loggedInUser = (User) loginResponse.getEntity();
        assertEquals(id, loggedInUser.getId());
        assertEquals("loginResUser", loggedInUser.getUsername());
        assertNull(loggedInUser.getPassword());
    }

    @Test
    public void testLoginEndpointFailure() {
        User newUser = new User("loginFailResUser", "correctPass");
        usersResource.createUser(newUser);

        User loginAttempt = new User("loginFailResUser", "wrongPass");
        Response loginResponse = usersResource.login(loginAttempt);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), loginResponse.getStatus());
        String msg = (String) loginResponse.getEntity();
        assertTrue(msg.contains("Login fehlgeschlagen"));
    }
}
