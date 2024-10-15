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

        //Costumer customer123 = dbManager.deleteCustomerById(UUID.fromString("87e3e3b0-52c6-43d4-8347-b3af06da5680"));

        dbManager.deleteCustomerById(costumer1.getId());

        //dbManager.addNewCustomer(costumer1);

        //dbManager.updateCustomerById(costumer1.getId(), "Alper", "Caliskan", LocalDate.of(1999, 1, 19), String.valueOf(Gender.M));

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