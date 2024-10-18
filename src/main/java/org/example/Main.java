package org.example;
import enums.Gender;
import enums.KindOfMeter;
import interfaces.Costumer;
import interfaces.DatabaseConnection;
import interfaces.Reading;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Properties;
import java.util.UUID;
import interfaces.CrudCustomer;
import interfaces.CrudReading;

public class Main {

    public static void main(String[] args) {

        UUID customerId = UUID.fromString("5e842183-ea5a-4d66-b3ee-9cc3c166e101");

        UUID readingId = UUID.fromString("df769321-3343-48f3-a889-bdf537fac2c1");

        Costumer costumer1 = new Costumer(customerId, "ch", "C.", LocalDate.of(2000, 1, 1), Gender.M);

        Reading reading1 = new Reading(readingId, "this is not a test", costumer1, LocalDate.of(2007, 1, 4), KindOfMeter.STROM, 20.0, "test1", Boolean.TRUE);

        DatabaseConnection dbManager = DatabaseConnection.getInstance();
        CrudCustomer crudCustomer = new CrudCustomer();
        CrudReading crudReading = new CrudReading();

        dbManager.openConnection(getProperties());

        crudReading.readReading(readingId);

        dbManager.closeConnection();

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