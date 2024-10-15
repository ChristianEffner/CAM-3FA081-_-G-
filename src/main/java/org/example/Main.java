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

public class Main {
    public static void main(String[] args) {

        UUID id = UUID.fromString("6c2c6ba8-633f-45e2-a6d9-3b320989ede1");

        //UUID.randomUUID();



        Costumer costumer1 = new Costumer(id, "Christian", "E.", LocalDate.of(1999, 1, 19), Gender.M);

        DatabaseConnection dbManager = DatabaseConnection.getInstance();

        dbManager.openConnection(getProperties());


        dbManager.deleteCustomerById(costumer1.getId());
        //dbManager.updateCustomerById(costumer1);


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