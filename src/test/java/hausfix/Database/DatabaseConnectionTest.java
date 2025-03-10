package hausfix.Database;

import hausfix.entities.User;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.sql.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DatabaseConnectionTest {

    private DatabaseConnection dbConnection;
    private Properties properties;

    @BeforeEach
    void setUp() {
        // Hole die Singleton-Instanz
        dbConnection = DatabaseConnection.getInstance();
        // Typische H2-InMemory-Properties
        properties = new Properties();
        properties.setProperty("db.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        properties.setProperty("db.user", "sa");
        properties.setProperty("db.pw", "");
    }

    @AfterEach
    void tearDown() {
        // Falls die Connection offen ist, immer schließen
        dbConnection.closeConnection();
    }

    // -------------------------------------------------------------
    // 1) OPEN CONNECTION
    // -------------------------------------------------------------
    @Test
    void testOpenConnectionSuccess() {
        assertDoesNotThrow(() -> dbConnection.openConnection(properties));
        assertNotNull(dbConnection.connection,
                "Die Connection darf nicht null sein, wenn openConnection() erfolgreich war");
    }

    @Test
    void testOpenConnectionWithInvalidProperties() {
        // absichtlich falsche URL
        Properties invalidProps = new Properties();
        invalidProps.setProperty("db.url", "jdbc:h2:file:INVALID_PATH/xyz");
        invalidProps.setProperty("db.user", "sa");
        invalidProps.setProperty("db.pw", "");
        dbConnection.openConnection(invalidProps);
        assertNull(dbConnection.connection,
                "Connection sollte null sein, wenn openConnection() fehlschlägt");
    }

    // -------------------------------------------------------------
    // 2) CREATE ALL TABLES
    // -------------------------------------------------------------
    @Test
    void testCreateAllTablesSuccess() {
        dbConnection.openConnection(properties);
        assertDoesNotThrow(() -> dbConnection.createAllTables(),
                "createAllTables() sollte ohne Fehler durchlaufen");
    }

    @Test
    void testCreateAllTablesConnectionNull() {
        // connection gar nicht erst öffnen -> connection bleibt null
        dbConnection.connection = null;
        assertDoesNotThrow(() -> dbConnection.createAllTables(),
                "Bei null-Connection wird nur Abbruch gemeldet, kein Fehler");
    }

    // Mock, damit createStatement() bzw. statement.execute(...) eine SQLException wirft
    @Test
    void testCreateAllTablesSQLException() throws SQLException {
        dbConnection.openConnection(properties);
        Connection spyConnection = Mockito.spy(dbConnection.connection);
        Statement mockStatement = Mockito.mock(Statement.class);
        when(spyConnection.createStatement()).thenReturn(mockStatement);
        doThrow(new SQLException("Fake SQL Error in createAllTables"))
                .when(mockStatement).execute(anyString());
        dbConnection.connection = spyConnection;
        assertDoesNotThrow(() -> dbConnection.createAllTables(),
                "Die SQLException wird intern gefangen und nur geloggt");
    }

    // -------------------------------------------------------------
    // 3) TRUNCATE ALL TABLES
    // -------------------------------------------------------------
    @Test
    void testTruncateAllTablesSuccess() {
        dbConnection.openConnection(properties);
        dbConnection.createAllTables();
        assertDoesNotThrow(() -> dbConnection.truncateAllTables());
    }

    @Test
    void testTruncateAllTablesConnectionNull() {
        // Connection nicht öffnen, also null
        dbConnection.connection = null;
        assertDoesNotThrow(() -> dbConnection.truncateAllTables(),
                "truncateAllTables() bricht nur ab, wenn connection null ist");
    }

    // Testet den "nicht H2"-Zweig via Mock (TRUNCATE anstatt DELETE)
    @Test
    void testTruncateAllTablesNonH2() throws Exception {
        dbConnection.openConnection(properties);
        Connection mockConn = Mockito.mock(Connection.class);
        DatabaseMetaData mockMeta = Mockito.mock(DatabaseMetaData.class);
        Statement mockStmt = Mockito.mock(Statement.class);
        when(mockConn.getMetaData()).thenReturn(mockMeta);
        // Behaupten, es sei "MySQL" statt "H2"
        when(mockMeta.getDatabaseProductName()).thenReturn("MySQL");
        when(mockConn.createStatement()).thenReturn(mockStmt);
        dbConnection.connection = mockConn;
        dbConnection.truncateAllTables();
        verify(mockStmt).executeUpdate("TRUNCATE TABLE READING;");
        verify(mockStmt).executeUpdate("TRUNCATE TABLE CUSTOMER;");
    }

    @Test
    void testTruncateAllTablesSQLException() throws SQLException {
        dbConnection.openConnection(properties);
        Connection spyConn = Mockito.spy(dbConnection.connection);
        Statement mockStmt = Mockito.mock(Statement.class);
        when(spyConn.createStatement()).thenReturn(mockStmt);
        doThrow(new SQLException("Fake SQL Error in truncateAllTables"))
                .when(mockStmt).executeUpdate(anyString());
        dbConnection.connection = spyConn;
        assertDoesNotThrow(() -> dbConnection.truncateAllTables());
    }

    // -------------------------------------------------------------
    // 4) REMOVE ALL TABLES
    // -------------------------------------------------------------
    @Test
    void testRemoveAllTablesSuccess() {
        dbConnection.openConnection(properties);
        dbConnection.createAllTables();
        assertDoesNotThrow(() -> dbConnection.removeAllTables());
    }

    @Test
    void testRemoveAllTablesConnectionNull() {
        dbConnection.connection = null;
        assertDoesNotThrow(() -> dbConnection.removeAllTables(),
                "removeAllTables() bricht nur ab, kein Fehler");
    }

    @Test
    void testRemoveAllTablesSQLException() throws SQLException {
        dbConnection.openConnection(properties);
        Connection spyConn = Mockito.spy(dbConnection.connection);
        Statement mockStmt = Mockito.mock(Statement.class);
        when(spyConn.createStatement()).thenReturn(mockStmt);
        doThrow(new SQLException("Fake SQL Error in removeAllTables"))
                .when(mockStmt).executeUpdate(anyString());
        dbConnection.connection = spyConn;
        assertDoesNotThrow(() -> dbConnection.removeAllTables());
    }

    // -------------------------------------------------------------
    // 5) CLOSE CONNECTION
    // -------------------------------------------------------------
    @Test
    void testCloseConnectionSuccess() {
        dbConnection.openConnection(properties);
        assertNotNull(dbConnection.connection, "Connection sollte vorhanden sein");
        dbConnection.closeConnection();
        assertNull(dbConnection.connection, "Nach closeConnection() sollte connection null sein");
    }

    @Test
    void testCloseConnectionTwice() {
        dbConnection.openConnection(properties);
        dbConnection.closeConnection();
        // Zweiter Aufruf => "Connection is already closed" wird ausgegeben, aber kein Fehler
        dbConnection.closeConnection();
    }

    // Neuer Test: Exception beim Schließen der Connection
    @Test
    void testCloseConnectionException() throws SQLException {
        dbConnection.openConnection(properties);
        // Erzeuge einen Spy, der beim close() absichtlich eine SQLException wirft
        Connection spyConn = Mockito.spy(dbConnection.connection);
        doThrow(new SQLException("Close failed")).when(spyConn).close();
        dbConnection.connection = spyConn;
        assertDoesNotThrow(() -> dbConnection.closeConnection(),
                "closeConnection() sollte Exceptions intern abfangen");
        // Da close() fehlschlägt, bleibt die Connection erhalten
        assertNotNull(dbConnection.connection, "Connection sollte erhalten bleiben, wenn close() fehlschlägt");
    }

    // -------------------------------------------------------------
    // 6) SAVE ENTITY
    // -------------------------------------------------------------
    @Test
    void testSaveUserEntitySuccess() throws SQLException {
        dbConnection.openConnection(properties);
        dbConnection.createAllTables();
        // Leere Tabelle users (H2: DELETE)
        try (Statement stmt = dbConnection.connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM `users`");
            dbConnection.connection.commit();
        }
        User user = new User("saveTestUser", "saveTestPass");
        dbConnection.save(user);
        // Überprüfe, ob der Benutzer in der DB gespeichert wurde
        try (PreparedStatement stmt = dbConnection.connection.prepareStatement("SELECT * FROM `users` WHERE username = ?")) {
            stmt.setString(1, "saveTestUser");
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "User sollte in der Datenbank gespeichert worden sein.");
                assertEquals("saveTestUser", rs.getString("username"));
                assertEquals("saveTestPass", rs.getString("password"));
            }
        }
    }

    @Test
    void testSaveUnknownEntity() {
        dbConnection.openConnection(properties);
        // Aufruf mit einer unbekannten Entität (z. B. Integer)
        assertDoesNotThrow(() -> dbConnection.save(123),
                "Aufruf von save() mit einer unbekannten Entität sollte keinen Fehler werfen.");
    }

    // Neuer Test: save() mit fehlender Connection
    @Test
    void testSaveNoConnection() {
        dbConnection.connection = null;
        User user = new User("noConnUser", "noConnPass");
        assertDoesNotThrow(() -> dbConnection.save(user),
                "save() sollte ohne Connection keinen Fehler werfen.");
    }

    // Neuer Test: Exception in save() bei User (simulate SQLException beim prepareStatement)
    @Test
    void testSaveUserEntitySQLException() throws SQLException {
        dbConnection.openConnection(properties);
        dbConnection.createAllTables();
        User user = new User("failUser", "failPass");
        Connection spyConn = Mockito.spy(dbConnection.connection);
        when(spyConn.prepareStatement(anyString())).thenThrow(new SQLException("Fake exception"));
        dbConnection.connection = spyConn;
        assertDoesNotThrow(() -> dbConnection.save(user),
                "save() sollte Exceptions intern abfangen, wenn prepareStatement fehlschlägt");
    }
}
