package hausfix.Database;

import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

    public class DatabaseConnectionTest {
        Connection connection;
        private static DatabaseConnection INSTANCE;
        private DatabaseConnection dbConnection;
        private Properties properties;

        @BeforeEach
        void setUp() {
            dbConnection = DatabaseConnection.getInstance();
            properties = new Properties();
            properties.setProperty("db.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
            properties.setProperty("db.user", "sa");
            properties.setProperty("db.pw", "");
        }

        @AfterEach
        void tearDown() {
            dbConnection.closeConnection();
        }

        @Test
        void testOpenConnection() {
            Connection connection = DatabaseConnection.getInstance().connection;
            assertDoesNotThrow(() -> dbConnection.openConnection(properties), "Connection should be opened without exception");
            assertNotNull(dbConnection.connection, "Connection should not be null after opening");
        }

        @Test
        void testCreateAllTables() {
            Connection connection = DatabaseConnection.getInstance().connection;
            dbConnection.openConnection(properties);
            assertDoesNotThrow(() -> dbConnection.createAllTables(), "Tables should be created without exception");

            try (ResultSet rs = dbConnection.connection.getMetaData().getTables(null, null, "CUSTOMER", null)) {
                assertTrue(rs.next(), "Customer table should exist after creation");
            } catch (SQLException e) {
                fail("Exception while checking if the Customer table exists");
            }

            try (ResultSet rs = dbConnection.connection.getMetaData().getTables(null, null, "READING", null)) {
                assertTrue(rs.next(), "Reading table should exist after creation");
            } catch (SQLException e) {
                fail("Exception while checking if the Reading table exists");
            }
        }

        @Test
        void testTruncateAllTables() {
            Connection connection = DatabaseConnection.getInstance().connection;
            dbConnection.openConnection(properties);
            dbConnection.createAllTables();

            assertDoesNotThrow(() -> dbConnection.truncateAllTables(), "Tables should be truncated without exception");

            try (Statement stmt = dbConnection.connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM CUSTOMER");
                rs.next();
                assertEquals(0, rs.getInt(1), "Customer table should be empty after truncation");

                rs = stmt.executeQuery("SELECT COUNT(*) FROM READING");
                rs.next();
                assertEquals(0, rs.getInt(1), "Reading table should be empty after truncation");
            } catch (SQLException e) {
                fail("Exception while checking table content after truncation");
            }
        }

        @Test
        void testRemoveAllTables() {
            Connection connection = DatabaseConnection.getInstance().connection;
            dbConnection.openConnection(properties);
            dbConnection.createAllTables();

            assertDoesNotThrow(() -> dbConnection.removeAllTables(), "Tables should be dropped without exception");

            try (ResultSet rs = dbConnection.connection.getMetaData().getTables(null, null, "CUSTOMER", null)) {
                assertFalse(rs.next(), "Customer table should not exist after removal");
            } catch (SQLException e) {
                fail("Exception while checking if the Customer table exists after removal");
            }

            try (ResultSet rs = dbConnection.connection.getMetaData().getTables(null, null, "READING", null)) {
                assertFalse(rs.next(), "Reading table should not exist after removal");
            } catch (SQLException e) {
                fail("Exception while checking if the Reading table exists after removal");
            }
        }

        @Test
        void testCloseConnection() {
            dbConnection.openConnection(properties);
            dbConnection.closeConnection();

            assertNull(dbConnection.connection, "Connection should be null after closing");
        }
    }

