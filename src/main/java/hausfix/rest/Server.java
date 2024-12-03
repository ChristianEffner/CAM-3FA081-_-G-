package hausfix.rest;
import com.sun.net.httpserver.HttpServer;
import java.net.URI;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

public class Server {

    static HttpServer server = null;
    public static void startRestServer() {
        String url = "http://localhost:8080/";
        final String pack = "hausfix.resourcen";
        System.out.println("Starting server...");
        System.out.println("URL: " + url);
        final ResourceConfig resourceConfig = new ResourceConfig()
                .packages(pack);
        server = JdkHttpServerFactory.createHttpServer(URI.create(url), resourceConfig);
        System.out.println("Ready for requests");
    }

    public static void stopServer() {
        if (server != null) server.stop(0);
        System.out.println("server stoped");
    }
}















