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

import CRUD.CRUDCustomer;
import interfaces.ID;

public class Main {
    public static void main(String[] args) {

        ID id = new ID();
        id.setId(UUID.randomUUID());

        Costumer costumer1 = new Costumer(id, "Chris", "Effner", LocalDate.of(1999, 1, 19), Gender.M);

        DatabaseConnection dbManager = new DatabaseConnection();

        dbManager.openConnection(getProperties());

        CRUDCustomer crudCustomer = new CRUDCustomer();
        crudCustomer.createCustomer(costumer1);

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