package hausfix.rest;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class StaticResouceHandlerTest {


    private File tempDir;
    private StaticResourceHandler handler;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("static-test").toFile();
        handler = new StaticResourceHandler(tempDir.getAbsolutePath());
    }

    @AfterEach
    void tearDown() {
        for (File file : tempDir.listFiles()) {
            file.delete();
        }
        tempDir.delete();
    }

    @Test
    void testServeIndexHtmlWhenPathEmpty() throws IOException {
        File index = new File(tempDir, "index.html");
        try (FileWriter writer = new FileWriter(index)) {
            writer.write("<html>Home</html>");
        }

        Response response = handler.serveFile("");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof File);
    }

    @Test
    void testServeFileNotFound() {
        Response response = handler.serveFile("notfound.txt");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("notfound.txt"));
    }

    @Test
    void testServeDirectoryInsteadOfFile() {
        File dir = new File(tempDir, "subdir");
        dir.mkdir();

        Response response = handler.serveFile("subdir");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("File not found"));
    }

    @Test
    void testServeFileWithIOException() {
        StaticResourceHandler faultyHandler = new StaticResourceHandler(tempDir.getAbsolutePath()) {
            @Override
            public Response serveFile(String path) {
                throw new RuntimeException("Simulierter Fehler");
            }
        };

        Exception exception = assertThrows(RuntimeException.class, () -> faultyHandler.serveFile("test.txt"));
        assertEquals("Simulierter Fehler", exception.getMessage());
    }
}
