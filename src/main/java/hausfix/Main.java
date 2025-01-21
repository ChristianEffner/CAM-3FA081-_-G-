package hausfix;
import com.sun.net.httpserver.HttpServer;
import hausfix.CRUD.CrudCustomer;
import hausfix.CRUD.CrudReading;
import hausfix.Database.DatabaseConnection;
import hausfix.entities.Customer;
import hausfix.entities.Reading;
import hausfix.enums.Gender;
import hausfix.enums.KindOfMeter;
import hausfix.rest.Server;
import org.glassfish.jersey.server.ResourceConfig;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Properties;
import java.util.UUID;
import org.glassfish.jersey.server.ResourceConfig;
import java.net.URI;

import static hausfix.enums.KindOfMeter.WASSER;

public class Main {
    public static void main(String[] args) throws SQLException {

        UUID customerId = UUID.fromString("1e160c14-84ac-476f-8e76-4053293c68c5");
        UUID readingId = UUID.fromString("5d50da4d-39fb-41d8-ad85-07ee956bd340");
        Customer customer1 = new Customer(customerId, "L", "K", LocalDate.of(2000, 11, 20), Gender.D);
        Reading reading1 = new Reading(readingId, "new test1", customer1, LocalDate.of(2005, 1, 1), KindOfMeter.HEIZUNG, 18.0, "test1", Boolean.FALSE);
        DatabaseConnection dbManager = DatabaseConnection.getInstance();
        CrudCustomer crudCustomerManager = new CrudCustomer();
        CrudReading crudReadingManager = new CrudReading();
        Server server = new Server();

        dbManager.openConnection(getProperties());

        //crudCustomerManager.readAllCustomers();

        crudReadingManager.getReadings(UUID.fromString("d71fe546-2917-4719-85c0-056bcf37ac1f"), LocalDate.of(1990, 1, 1), LocalDate.of(1990, 1, 1), KindOfMeter.STROM);

        server.startRestServer();

        //dbManager.closeConnection();

    }

    public static Properties getProperties() {
        Properties properties = new Properties();
        try {
            InputStream input = new FileInputStream("src/main/resources/config.properties");
            properties.load(input);
        } catch (IOException e) {
            System.out.println(e);
        }
        return properties;
    }
}