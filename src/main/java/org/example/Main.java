package org.example;
import enums.Gender;
import enums.KindOfMeter;
import interfaces.Costumer;
import interfaces.CrudCustomer;
import interfaces.DatabaseConnection;
import interfaces.Reading;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Properties;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {

        UUID customerId = UUID.fromString("5e18f47a-ffbb-4813-a813-b36be211c114");

        UUID readingId = UUID.fromString("7dd46fb0-b8e6-472f-8034-762b987d7f5a");

        CrudCustomer crudcustomer = new CrudCustomer();
        Costumer costumer1 = new Costumer(customerId, "Christian", "E.", LocalDate.of(1999, 1, 19), Gender.M);
        Reading reading1 = new Reading(readingId, "this is a test", costumer1, LocalDate.of(2005, 1, 1), KindOfMeter.HEIZUNG, 18.0, "test1", Boolean.FALSE);

        DatabaseConnection dbManager = DatabaseConnection.getInstance();

        dbManager.openConnection(getProperties());

        crudcustomer.addNewCustomer(costumer1);


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