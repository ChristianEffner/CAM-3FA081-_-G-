package hausfix;
import hausfix.CRUD.CrudCustomer;
import hausfix.CRUD.CrudReading;
import hausfix.SQL.DatabaseConnection;
import hausfix.entities.Customer;
import hausfix.entities.Reading;
import hausfix.enums.Gender;
import hausfix.enums.KindOfMeter;
import hausfix.rest.Server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Properties;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {

        UUID customerId = UUID.fromString("d7109982-a5e3-441f-86db-a5b59ac61f13");
        UUID readingId = UUID.randomUUID();

        Customer customer1 = new Customer(customerId, "Christian", "Effner", LocalDate.of(1999, 1, 20), Gender.M);
        Reading reading1 = new Reading(readingId, "hallo", customer1, LocalDate.of(2005, 1, 1), KindOfMeter.HEIZUNG, 18.0, "test1", Boolean.FALSE);

        DatabaseConnection dbManager = DatabaseConnection.getInstance();

        CrudCustomer crudCustomerManager = new CrudCustomer();
        CrudReading crudReadingManager = new CrudReading();
        Server restServer = new Server();

        restServer.startRestServer();
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