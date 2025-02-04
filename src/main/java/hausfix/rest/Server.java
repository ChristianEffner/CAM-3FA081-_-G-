package hausfix.rest;
import com.sun.net.httpserver.HttpServer;
import java.net.URI;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;


public class Server {

    static HttpServer server = null;

    public static void startRestServer() {
        if (server != null) {
            System.out.println("Server is already running.");
            return;
        }
        String url = "http://localhost:8080/";
        System.out.println("Starting server...");
        System.out.println("URL: " + url);

        // Registrierung von mehreren Paketen
        ResourceConfig rc = new ResourceConfig()
                .packages("hausfix.resourcen", "hausfix.rest") // Beide Pakete registrieren
                .register(StaticResourceHandler.class);       // Statische Ressourcen

        server = JdkHttpServerFactory.createHttpServer(URI.create(url), rc);
        System.out.println("Ready for requests");
    }



    public static void stopServer() {
        if (server != null) {
            System.out.println("Stopping server...");
            server.stop(3); // Stop with a graceful shutdown
            server = null; // Set the server reference to null to indicate it has been stopped
            System.out.println("Server stopped.");
        } else {
            System.out.println("No server to stop.");
        }
    }

    public static HttpServer getServer() {
        return server;
    }

}















