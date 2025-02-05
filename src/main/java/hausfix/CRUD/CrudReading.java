package hausfix.CRUD;

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
     * Jede Reading wird mit einem Customer verknüpft, der für den aktuell aktiven User existieren muss.
     * Falls der Customer noch nicht existiert, wird er anhand der ICustomer-Daten erstellt und
     * dem aktiven User zugeordnet. Zusätzlich wird das Feld user_id in der Reading-Tabelle gesetzt.
     *
     * WICHTIG: Damit der Fremdschlüssel (user_id) gültig ist, muss in der Tabelle `users`
     * ein Datensatz mit der entsprechenden ID existieren – zum Beispiel ein Dummy-Benutzer,
     * den Du in Deiner Setup-Phase anlegst.
     */
    public void addNewReading(Reading reading) {
        // Prüfe, ob ein Customer übergeben wurde
        if (reading.getCustomer() == null) {
            throw new IllegalArgumentException("Es muss ein Customer angegeben werden.");
        }

        // Hole das ICustomer-Objekt und konvertiere es in ein Customer-Objekt
        ICustomer iCust = reading.getCustomer();
        Customer customerObj;
        if (iCust instanceof Customer) {
            customerObj = (Customer) iCust;
        } else {
            customerObj = new Customer(iCust.getId(), iCust.getFirstName(), iCust.getLastName(),
                    iCust.getBirthDate(), iCust.getGender());
        }

        // Setze exemplarisch den aktuell aktiven User (hier: activeUserId = 1L; in echter Anwendung dynamisch ermitteln)
        Long activeUserId = 1L;
        if (customerObj.getUserId() == null) {
            customerObj.setUserId(activeUserId);
        }

        // Prüfe, ob der Kunde bereits existiert
        Customer existingCustomer = crudCustomer.readCustomer(customerObj.getId());
        if (existingCustomer == null) {
            try {
                crudCustomer.addNewCustomer(customerObj);
                System.out.println("Neuer Kunde mit ID " + customerObj.getId() + " wurde erstellt.");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Fehler beim Anlegen des Kunden: " + e.getMessage(), e);
            }
        } else {
            customerObj = existingCustomer;
        }
        // Setze den Customer im Reading-Objekt
        reading.setCustomer(customerObj);

        // Ermittle den user_id-Wert für das Reading (entspricht dem des zugehörigen Customers)
        Long userIdForReading = customerObj.getUserId();

        // WICHTIG: Stelle sicher, dass in der Tabelle `users` ein Datensatz mit user_id = userIdForReading existiert!
        // Falls nicht, schlägt das Insert fehl.

        // Führe den Insert für das Reading aus (inklusive user_id)
        String query = "INSERT INTO Reading (id, comment, customer_id, date_of_reading, kind_of_meter, meter_count, meter_id, substitute, user_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().connection;

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, reading.getId().toString());
            preparedStatement.setString(2, reading.getComment());
            preparedStatement.setString(3, reading.getCustomer().getId().toString());
            preparedStatement.setDate(4, java.sql.Date.valueOf(reading.getDateOfReading()));
            preparedStatement.setString(5, reading.getKindOfMeter().toString());
            preparedStatement.setDouble(6, reading.getMeterCount());
            preparedStatement.setString(7, reading.getMeterId());
            preparedStatement.setBoolean(8, reading.getSubstitute());
            if (userIdForReading != null) {
                preparedStatement.setLong(9, userIdForReading);
            } else {
                preparedStatement.setNull(9, java.sql.Types.BIGINT);
            }
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Reading mit ID " + reading.getId() + " wurde erfolgreich eingefügt.");
            } else {
                throw new RuntimeException("Insert von Reading mit ID " + reading.getId() + " war nicht erfolgreich.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Fehler beim Einfügen des Readings: " + e.getMessage(), e);
        }
    }

    /**
     * Liest alle Readings (ohne Filter).
     */
    public List<Reading> readAllReading() {
        String selectAllReadings = "SELECT * FROM Reading;";
        Connection connection = DatabaseConnection.getInstance().connection;
        List<Reading> readings = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(selectAllReadings)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Reading r = mapRowToReading(resultSet);
                if (r != null) {
                    readings.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return readings;
    }

    /**
     * Liest alle Readings für einen bestimmten User anhand des Feldes user_id.
     */
    public List<Reading> readAllReadingForUser(Long userId) {
        String sql = "SELECT * FROM Reading WHERE user_id = ?";
        List<Reading> list = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().connection;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reading r = mapRowToReading(rs);
                if (r != null) {
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Liest ein Reading anhand der ID.
     */
    public static Reading readReading(UUID id) {
        String selectReading = "SELECT * FROM Reading WHERE id = ?;";
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
                Long userId = resultSet.getLong("user_id");
                if (resultSet.wasNull()) {
                    userId = null;
                }
                // Erzeuge einen Customer-Dummy – idealerweise wird hier der tatsächliche Customer geladen
                Customer customer = new Customer(UUID.fromString(cust_id), "John", "Doe", LocalDate.now(), Gender.M);
                customer.setUserId(userId);
                return new Reading(
                        id,
                        comment,
                        customer,
                        LocalDate.parse(date_of_reading),
                        KindOfMeter.valueOf(kind_of_meter),
                        Double.parseDouble(meter_count),
                        meter_id,
                        Boolean.parseBoolean(substitute),
                        userId
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Löscht das Reading mit gegebener ID aus der DB.
     */
    public Reading deleteReadingById(UUID readingId) {
        // Implementierung ergänzen, falls benötigt
        return null;
    }

    /**
     * Aktualisiert ein Reading in der DB anhand seiner ID.
     */
    public void updateReadingById(Reading reading) {
        // Implementierung ergänzen, falls benötigt
    }

    /**
     * Wandelt ein ResultSet in ein Reading um.
     */
    private Reading mapRowToReading(ResultSet resultSet) throws SQLException {
        String idStr = resultSet.getString("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            System.err.println("Warnung: Ein Reading-Datensatz hat NULL/leer als ID. Überspringe Datensatz.");
            return null;
        }
        UUID id = UUID.fromString(idStr);
        String comment = resultSet.getString("comment");
        String custIdStr = resultSet.getString("customer_id");
        if (custIdStr == null || custIdStr.trim().isEmpty()) {
            System.err.println("Warnung: Ein Reading-Datensatz hat NULL/leer als customer_id. Überspringe Datensatz.");
            return null;
        }
        UUID customer_id = UUID.fromString(custIdStr);
        LocalDate date_of_reading = resultSet.getDate("date_of_reading").toLocalDate();
        KindOfMeter kindOfMeter = KindOfMeter.valueOf(resultSet.getString("kind_of_meter"));
        double meter_count = resultSet.getDouble("meter_count");
        String meter_id = resultSet.getString("meter_id");
        boolean substitute = resultSet.getBoolean("substitute");
        // Hole den echten Customer
        Customer realCustomer = crudCustomer.readCustomer(customer_id);
        if (realCustomer == null) {
            realCustomer = new Customer(customer_id, "John", "Doe", LocalDate.now(), Gender.M);
        }
        Long userId = resultSet.getLong("user_id");
        if (resultSet.wasNull()) {
            userId = null;
        }
        return new Reading(
                id,
                comment,
                realCustomer,
                date_of_reading,
                kindOfMeter,
                meter_count,
                meter_id,
                substitute,
                userId
        );
    }
}
