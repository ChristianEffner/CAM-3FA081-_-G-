package org.example;
import enums.Gender;
import enums.KindOfMeter;
import interfaces.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Properties;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {

        UUID customerId = UUID.fromString("43847338-8a5d-44f4-9477-e4789831899f");
        UUID readingId = UUID.fromString("7dd46fb0-b8e6-472f-8034-762b987d7f5a");

        // Create a customer and a reading
        Costumer costumer1 = new Costumer(customerId, "A", "o.", LocalDate.of(1999, 1, 19), Gender.M);
        Reading reading1 = new Reading(readingId, "hallo", costumer1, LocalDate.of(2005, 1, 1), KindOfMeter.HEIZUNG, 18.0, "test1", Boolean.FALSE);

        // Get the database connection instance
        DatabaseConnection dbManager = DatabaseConnection.getInstance();
        // Create a new CrudCustomer instance
        CrudCustomer crudCustomerManager = new CrudCustomer();
        CrudReading crudReadingManager = new CrudReading();

        // Open the database connection using properties from the config file
        dbManager.openConnection(getProperties());

        // Add the new customer to the database
        crudReadingManager.deleteReadingById(readingId);

        // Close the database connection
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