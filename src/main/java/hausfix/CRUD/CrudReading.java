package hausfix.CRUD;

import hausfix.CRUD.CrudCustomer;
import hausfix.entities.Customer;
import hausfix.entities.Reading;
import hausfix.enums.Gender;
import hausfix.enums.KindOfMeter;
import hausfix.Database.DatabaseConnection;
import hausfix.interfaces.ICustomer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrudReading extends DatabaseConnection {

    private CrudCustomer crudCustomer = new CrudCustomer();

    /**
     * Fügt eine neue Ablesung (Reading) in die DB ein.
     * Stellt sicher, dass "reading.getId()" und "reading.getCustomer().getId()" nicht null sind.
     */
    public void addNewReading(Reading reading) {
        // Wenn reading.getId() null => generiere neue UUID
        if (reading.getId() == null) {
            reading.setId(UUID.randomUUID());
        }
        // Bei Customer-ID genauso
        if (reading.getCustomer() != null && reading.getCustomer().getId() == null) {
            reading.getCustomer().setId(UUID.randomUUID());
        }

        String query = "INSERT INTO reading (id, comment, customer_id, date_of_reading, kind_of_meter, meter_count, meter_id, substitute) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // DB-Connection
        Connection connection = DatabaseConnection.getInstance().connection;

        try {
            UUID customerId = reading.getCustomer().getId();
            // Prüfen, ob dieser Kunde existiert
            Customer existingCustomer = crudCustomer.readCustomer(customerId);

            if (existingCustomer == null) {
                // Falls der Kunde nicht existiert, lege ihn an
                ICustomer iCustomer = reading.getCustomer();
                // Casting, falls "reading.getCustomer()" wirklich eine Customer-Instanz ist
                if (iCustomer instanceof Customer) {
                    Customer newCustomer = (Customer) iCustomer;
                    crudCustomer.addNewCustomer(newCustomer);
                }
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                // Setze alle Parameter
                preparedStatement.setString(1, reading.getId().toString());
                preparedStatement.setString(2, reading.getComment());
                preparedStatement.setString(3, reading.getCustomer().getId().toString());
                preparedStatement.setDate(4, java.sql.Date.valueOf(reading.getDateOfReading()));
                preparedStatement.setString(5, reading.getKindOfMeter().toString());
                preparedStatement.setDouble(6, reading.getMeterCount());
                preparedStatement.setString(7, reading.getMeterId());
                preparedStatement.setBoolean(8, reading.getSubstitute());

                preparedStatement.executeUpdate();
                System.out.println("Record successfully inserted!");
            } catch (SQLException e) {
                System.err.println("Error inserting record: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error adding a new reading: " + e.getMessage());
        }
    }

    /**
     * Liest ALLE Readings aus der Tabelle "reading".
     * Verhindert NullPointerException bei UUID.fromString(null) => Wenn Spalte "id" oder "customer_id" null/leer ist,
     * überspringen wir diesen Datensatz. Du kannst das nach Belieben anpassen.
     */
    public List<Reading> readAllReading() {
        String selectAllReadings = "SELECT * FROM reading;";
        Connection connection = DatabaseConnection.getInstance().connection;
        List<Reading> readings = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(selectAllReadings)) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                // --> ID
                String idStr = resultSet.getString("id");
                if (idStr == null || idStr.trim().isEmpty()) {
                    System.err.println("Warnung: Ein Reading-Datensatz hat NULL/leer als ID. Überspringe Datensatz.");
                    continue;
                }
                UUID id = UUID.fromString(idStr);

                // --> Comment
                String comment = resultSet.getString("comment");

                // --> Customer_ID
                String custIdStr = resultSet.getString("customer_id");
                if (custIdStr == null || custIdStr.trim().isEmpty()) {
                    System.err.println("Warnung: Ein Reading-Datensatz hat NULL/leer als customer_id. Überspringe Datensatz.");
                    continue;
                }
                UUID customer_id = UUID.fromString(custIdStr);

                // --> Date
                LocalDate date_of_reading = resultSet.getDate("date_of_reading").toLocalDate();

                // --> KindOfMeter
                KindOfMeter kindOfMeter = KindOfMeter.valueOf(resultSet.getString("kind_of_meter"));

                // --> meter_count
                double meter_count = resultSet.getDouble("meter_count");

                // --> meter_id
                String meter_id = resultSet.getString("meter_id");

                // --> substitute
                boolean substitute = resultSet.getBoolean("substitute");

                // Beispielhaft erstellst du hier "Dummy"-Kunde (John Doe)
                // Da du "readCustomer(customer_id)" rufen könntest, um den echten Kundendatensatz zu laden,
                // mache das ruhig, falls gewünscht.
                // Zum Beispiel:
                Customer realCustomer = crudCustomer.readCustomer(customer_id);
                if (realCustomer == null) {
                    // Falls er nicht existiert, nimm dummy
                    realCustomer = new Customer(customer_id, "John", "Doe", LocalDate.now(), Gender.M);
                }

                // Reading-Objekt bauen
                Reading reading1 = new Reading(
                        id,
                        comment,
                        realCustomer,
                        date_of_reading,
                        kindOfMeter,
                        meter_count,
                        meter_id,
                        substitute
                );
                readings.add(reading1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return readings;
    }

    /**
     * Liest EIN Reading anhand der ID (SELECT * FROM reading WHERE id=?).
     */
    public static Reading readReading(UUID id) {
        String selectReading = "SELECT * FROM reading WHERE id = ?;";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (PreparedStatement statement = connection.prepareStatement(selectReading)) {
            statement.setObject(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String comment = resultSet.getString("comment");
                String cust_id = resultSet.getString("customer_id");
                String date_of_reading = resultSet.getString("date_of_reading");
                String kind_of_meter = resultSet.getString("kind_of_meter");
                String meter_count = resultSet.getString("meter_count");
                String meter_id = resultSet.getString("meter_id");
                String substitute = resultSet.getString("substitute");

                // Customer-Objekt: hier wieder Dummy-Werte
                // Oder du nutzt CrudCustomer, um den echten Kunden zu laden
                Customer customer = new Customer(UUID.fromString(cust_id), "John", "Doe", LocalDate.now(), Gender.M);

                // Reading-Objekt bauen und zurückgeben
                return new Reading(
                        id,
                        comment,
                        customer,
                        LocalDate.parse(date_of_reading),
                        KindOfMeter.valueOf(kind_of_meter),
                        Double.parseDouble(meter_count),
                        meter_id,
                        Boolean.parseBoolean(substitute)
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Fallback, falls keine Daten gefunden werden
    }

    /**
     * Löscht das Reading mit gegebener ID aus der DB.
     */
    public Reading deleteReadingById(UUID readingId) {
        String deleteReadingSQL = "DELETE FROM reading WHERE id = ?;";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (PreparedStatement statement = connection.prepareStatement(deleteReadingSQL)) {
            statement.setObject(1, readingId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Reading with ID " + readingId + " was deleted successfully.");
            } else {
                System.out.println("No reading found with ID " + readingId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Aktualisiert ein Reading-Objekt in der Datenbank anhand seiner ID.
     */
    public void updateReadingById(Reading reading) {
        String updateReadingSQL = "UPDATE reading "
                + "SET comment = ?, customer_id = ?, date_of_reading = ?, kind_of_meter = ?, meter_count = ?, meter_id = ?, substitute = ? "
                + "WHERE id = ?;";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateReadingSQL)) {

            preparedStatement.setString(1, reading.getComment());
            preparedStatement.setString(2, reading.getCustomer().getId().toString());
            preparedStatement.setDate(3, java.sql.Date.valueOf(reading.getDateOfReading()));
            preparedStatement.setString(4, reading.getKindOfMeter().toString());
            preparedStatement.setDouble(5, reading.getMeterCount());
            preparedStatement.setString(6, reading.getMeterId());
            preparedStatement.setBoolean(7, reading.getSubstitute());
            preparedStatement.setObject(8, reading.getId());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Reading with ID " + reading.getId() + " was updated successfully.");
            } else {
                System.out.println("No reading found with ID " + reading.getId());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
