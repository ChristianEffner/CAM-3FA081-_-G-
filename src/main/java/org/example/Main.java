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