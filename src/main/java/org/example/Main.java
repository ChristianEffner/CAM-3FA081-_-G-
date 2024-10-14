package org.example;
import enums.Gender;
import interfaces.Costumer;
import interfaces.DatabaseConnection;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Properties;
import java.util.UUID;
import interfaces.ID;

public class Main {
    public static void main(String[] args) {

        UUID id = UUID.randomUUID();

        Costumer costumer1 = new Costumer(id, "Chris", "Effner", LocalDate.of(1999, 1, 19), Gender.M);

        DatabaseConnection dbManager = DatabaseConnection.getInstance();

        dbManager.openConnection(getProperties());

        Costumer customer123 = dbManager.readCustomer(UUID.fromString("bc7b2eb0-cb77-40fd-bd26-05d5c4e76b43"));

        System.out.println(customer123.getFirstName());

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