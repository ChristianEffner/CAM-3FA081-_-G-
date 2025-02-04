package hausfix;

import hausfix.CRUD.CrudCustomer;
import hausfix.CRUD.CrudReading;
import hausfix.Database.DatabaseConnection;
import hausfix.entities.Customer;
import hausfix.entities.Reading;
import hausfix.enums.Gender;
import hausfix.enums.KindOfMeter;
import hausfix.rest.Server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Properties;
import java.util.UUID;

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

        // 1) Verbindung öffnen
        dbManager.openConnection(getProperties());
        System.out.println("Datenbankverbindung geöffnet.");

        // 3) REST-Server starten
        Server.startRestServer();
        System.out.println("REST-Server gestartet.");
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
