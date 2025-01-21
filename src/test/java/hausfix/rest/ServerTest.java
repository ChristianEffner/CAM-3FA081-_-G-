package hausfix.rest;

import com.sun.net.httpserver.HttpServer;
import hausfix.Database.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;

import static hausfix.Main.getProperties;
import static hausfix.rest.Server.server;
import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    private Connection connection;

    // Vor jedem Test starten wir den Server
    @BeforeEach
    public void setUp() {
        DatabaseConnection dbManager = DatabaseConnection.getInstance();
        connection = (Connection) dbManager.openConnection(getProperties());
        Server server1 = new Server();
        server1.startRestServer();
    }

    // Nach jedem Test stoppen wir den Server
    @AfterEach
    public void tearDown() {
        Server server1 = new Server();
        server1.stopServer();
    }

    @Test
    void testStartRestServer() throws Exception {

        Connection connection = DatabaseConnection.getInstance().connection;
        // Testen, ob der Server erfolgreich gestartet wurde
        URL url = new URL("http://localhost:8080/customers");
        HttpURLConnection connection1 = (HttpURLConnection) url.openConnection();

        // Setze die HTTP-Methode auf GET, um eine Anfrage zu senden
        connection1.setRequestMethod("GET");

        // Hole den Statuscode der Antwort
        int statusCode = connection1.getResponseCode();

        // Überprüfe, ob der Statuscode OK (200) ist
        assertEquals(200, statusCode, "Server should be running and respond with HTTP 200 OK");

        // Lese den Response Body
        try (Reader reader = new InputStreamReader(connection1.getInputStream())) {
            int data = reader.read();
            assertTrue(data != -1, "Response should not be empty.");
        }
    }

    @Test
    void testServerAlreadyRunning() {

        Server server1 = new Server();
        // Test, wenn der Server bereits läuft, dass keine neuen Instanzen gestartet werden
        HttpServer initialServer = server1.getServer();

        // Versuche, den Server erneut zu starten
        server1.startRestServer();

        // Der Server sollte nicht neu gestartet werden
        assertSame(initialServer, server1.getServer(), "Server should not be restarted if already running.");
    }
}

