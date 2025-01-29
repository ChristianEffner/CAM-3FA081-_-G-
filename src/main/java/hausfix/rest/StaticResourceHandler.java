package hausfix.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.nio.file.Files;

@Path("/")
public class StaticResourceHandler {

    private final String baseDirectory;

    public StaticResourceHandler() {
        // Dynamically resolve base directory to the correct path
        this.baseDirectory = System.getProperty("user.dir") + "/src/main/webapp";
    }

    @GET
    @Path("{path:.*}") // Match any path
    @Produces(MediaType.WILDCARD)
    public Response serveFile(@PathParam("path") String path) {
        try {
            // Default to index.html if the path is empty
            if (path == null || path.isEmpty()) {
                path = "index.html";
            }

            // Resolve the file path
            File file = new File(baseDirectory, path);

            // Check if the file exists and is not a directory
            if (file.exists() && !file.isDirectory()) {
                String mimeType = Files.probeContentType(file.toPath());
                return Response.ok(file, mimeType).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("File not found: " + path)
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal server error: " + e.getMessage())
                    .build();
        }
    }
}