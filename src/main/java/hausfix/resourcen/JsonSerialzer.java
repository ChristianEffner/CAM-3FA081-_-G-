package hausfix.resourcen;

import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;

@Provider
public class JsonSerialzer extends JacksonJaxbJsonProvider {
}
