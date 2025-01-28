package hausfix.Database;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.sql.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseConnectionTest {

    private DatabaseConnection dbConnection;
    private Properties properties;

    @BeforeEach
    void setUp() {
        // Singleton-Instanz holen
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
        // Sollte klappen, da "jdbc:h2:mem:..."
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

        // Catch-Block in openConnection() -> wird nur geloggt, connection bleibt null
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
        assertDoesNotThrow(() -> dbConnection.createAllTables(),
                "Bei null-Connection wird nur Abbruch gemeldet, kein Fehler");
    }

    // Mock, damit createStatement() bzw. statement.execute(...) eine SQLException wirft
    @Test
    void testCreateAllTablesSQLException() throws SQLException {
        // 1) Normal öffnen
        dbConnection.openConnection(properties);

        // 2) Spy/Mocks bauen
        Connection spyConnection = Mockito.spy(dbConnection.connection);
        Statement mockStatement = Mockito.mock(Statement.class);

        // 3) createStatement() soll unser Mock zurückgeben
        when(spyConnection.createStatement()).thenReturn(mockStatement);
        // 4) Wenn execute(...) aufgerufen wird, fliegt absichtlich eine SQLException
        doThrow(new SQLException("Fake SQL Error in createAllTables"))
                .when(mockStatement).execute(anyString());

        // 5) Spy injizieren
        dbConnection.connection = spyConnection;

        // 6) Aufruf -> die SQLException wird gefangen, e.printStackTrace() im Code
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
        assertDoesNotThrow(() -> dbConnection.truncateAllTables(),
                "truncateAllTables() bricht nur ab, wenn connection null ist");
    }

    // Testet den "nicht H2"-Zweig via Mock (TRUNCATE anstatt DELETE)
    @Test
    void testTruncateAllTablesNonH2() throws Exception {
        // 1) Verbindung öffnen
        dbConnection.openConnection(properties);

        // 2) Mock für Connection, MetaData, Statement
        Connection mockConn = Mockito.mock(Connection.class);
        DatabaseMetaData mockMeta = Mockito.mock(DatabaseMetaData.class);
        Statement mockStmt = Mockito.mock(Statement.class);

        when(mockConn.getMetaData()).thenReturn(mockMeta);
        // Behaupten, es sei "MySQL" statt "H2"
        when(mockMeta.getDatabaseProductName()).thenReturn("MySQL");
        when(mockConn.createStatement()).thenReturn(mockStmt);

        // 3) injizieren
        dbConnection.connection = mockConn;

        // 4) Aufruf -> sollte in den if-Zweig gehen (TRUNCATE TABLE)
        dbConnection.truncateAllTables();

        // 5) verifizieren, dass das Mock-Statement TRUNCATE aufgerufen hat
        verify(mockStmt).executeUpdate("TRUNCATE TABLE READING;");
        verify(mockStmt).executeUpdate("TRUNCATE TABLE CUSTOMER;");
    }

    @Test
    void testTruncateAllTablesSQLException() throws SQLException {
        // 1) Öffnen
        dbConnection.openConnection(properties);

        // 2) Spy auf Connection
        Connection spyConn = Mockito.spy(dbConnection.connection);
        Statement mockStmt = Mockito.mock(Statement.class);

        // 3) createStatement -> mock
        when(spyConn.createStatement()).thenReturn(mockStmt);
        doThrow(new SQLException("Fake SQL Error in truncateAllTables"))
                .when(mockStmt).executeUpdate(anyString());

        // 4) injizieren
        dbConnection.connection = spyConn;

        // 5) Aufruf -> sollte catch block erwischen
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
        // connection gar nicht öffnen -> null
        assertDoesNotThrow(() -> dbConnection.removeAllTables(),
                "removeAllTables() bricht nur ab, kein Fehler");
    }

    @Test
    void testRemoveAllTablesSQLException() throws SQLException {
        // 1) Öffnen
        dbConnection.openConnection(properties);

        // 2) Spy
        Connection spyConn = Mockito.spy(dbConnection.connection);
        Statement mockStmt = Mockito.mock(Statement.class);

        when(spyConn.createStatement()).thenReturn(mockStmt);
        doThrow(new SQLException("Fake SQL Error in removeAllTables"))
                .when(mockStmt).executeUpdate(anyString());

        dbConnection.connection = spyConn;

        // catch-Block wird intern ausgelöst, test bleibt grün
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
        // Nochmal schließen => "Connection is already closed"
        dbConnection.closeConnection();
        // kein Fehler, else-Zweig wird erreicht
    }
}
