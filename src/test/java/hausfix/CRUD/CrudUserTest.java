package hausfix.CRUD;

import hausfix.Database.DatabaseConnection;
import hausfix.entities.User;
import hausfix.security.PasswordUtil;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;

import java.sql.*;
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
        dbManager.connection = connection;

        crudUser = new CrudUser();

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

    @AfterEach
    public void resetDbConnection() throws SQLException {
        if (DatabaseConnection.getInstance().connection == null) {
            Connection restored = DatabaseConnection.getInstance().openConnection(getProperties());
            DatabaseConnection.getInstance().connection = restored;
        }
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
        assertEquals("{\"message\":\"Benutzer aktualisiert\"}", updateMsg);

        Response getResponse = crudUser.getUserById(userId);
        User retrievedUser = (User) getResponse.getEntity();
        assertEquals("updatedUser", retrievedUser.getUsername());
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
    public void testLoginUnknownUser() {
        User loginAttempt = new User("ghostUser", "anyPassword");
        Response loginResponse = crudUser.login(loginAttempt);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), loginResponse.getStatus());
        String msg = (String) loginResponse.getEntity();
        assertTrue(msg.contains("Login fehlgeschlagen"));
    }

    @Test
    public void testGetAllUsersDbUnavailable() {
        DatabaseConnection.getInstance().connection = null;
        Response response = crudUser.getAllUsers();
        assertEquals(503, response.getStatus());
        assertTrue(response.getEntity().toString().contains("DB-Verbindung nicht verfügbar"));
    }

    @Test
    public void testUpdateUserWithPassword() {
        User newUser = new User("pwUser", "start123");
        Response createResp = crudUser.createUser(newUser);
        User created = (User) createResp.getEntity();

        User updated = new User("pwUserUpdated", "newSecret123");
        Response response = crudUser.updateUser(created.getId(), updated);

        assertEquals(200, response.getStatus());
        assertEquals("{\"message\":\"Benutzer aktualisiert\"}", response.getEntity());
    }

    @Test
    public void testUpdateUserWithoutPassword() {
        User newUser = new User("noPwUser", "start123");
        Response createResp = crudUser.createUser(newUser);
        User created = (User) createResp.getEntity();

        User updated = new User("noPwUserUpdated", null);  // kein Passwort gesetzt
        Response response = crudUser.updateUser(created.getId(), updated);

        assertEquals(200, response.getStatus());
        assertEquals("{\"message\":\"Benutzer aktualisiert\"}", response.getEntity());
    }

    @Test
    public void testUpdateUserDbUnavailable() {
        var db = DatabaseConnection.getInstance();
        Connection original = db.connection;

        db.connection = null; // Simuliere Verbindungsverlust
        User dummy = new User("irrelevant", "irrelevant");
        Response response = crudUser.updateUser(1L, dummy);
        assertEquals(503, response.getStatus());
        assertTrue(response.getEntity().toString().contains("DB-Verbindung nicht verfügbar"));

        db.connection = original; // Wiederherstellen
    }

    @Test
    public void testUpdateUserSqlException() throws Exception {
        // Simuliere SQL-Fehler durch geschlossene Connection
        Connection faulty = DriverManager.getConnection("jdbc:h2:mem:baddb;DB_CLOSE_DELAY=-1");
        faulty.close(); // absichtlich schließen

        DatabaseConnection db = DatabaseConnection.getInstance();
        Connection original = db.connection;
        db.connection = faulty;

        try {
            User dummy = new User("failUpdate", "failPw");
            Response response = crudUser.updateUser(1L, dummy);

            assertEquals(500, response.getStatus(), "Es wird ein Serverfehler erwartet");
            assertNotNull(response.getEntity(), "Die Fehlermeldung sollte nicht null sein");
            assertTrue(response.getEntity().toString().toLowerCase().contains("error")
                            || response.getEntity().toString().toLowerCase().contains("exception"),
                    "Fehlermeldung sollte 'error' oder 'exception' enthalten: " + response.getEntity());
        } finally {
            db.connection = original; // Verbindung wiederherstellen
        }
    }

    @Test
    void testGetUserById_Success() {
        User user = new User("test_get", "pass");
        Response createResp = crudUser.createUser(user);
        User created = (User) createResp.getEntity();

        Response response = crudUser.getUserById(created.getId());
        assertEquals(200, response.getStatus());
        User returned = (User) response.getEntity();
        assertEquals(created.getId(), returned.getId());
        assertEquals("test_get", returned.getUsername());
        assertNull(returned.getPassword()); // Sicherheit
    }

    @Test
    void testGetUserById_NotFound() {
        Response response = crudUser.getUserById(999999L); // nicht existierend
        assertEquals(404, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Benutzer nicht gefunden"));
    }

    @Test
    void testGetUserById_DbUnavailable() {
        DatabaseConnection db = DatabaseConnection.getInstance();
        Connection original = db.connection;
        db.connection = null; // Verbindung deaktivieren

        try {
            Response response = crudUser.getUserById(1L);
            assertEquals(503, response.getStatus());
            assertTrue(response.getEntity().toString().contains("DB-Verbindung nicht verfügbar"));
        } finally {
            db.connection = original; // Wiederherstellen
        }
    }





}
