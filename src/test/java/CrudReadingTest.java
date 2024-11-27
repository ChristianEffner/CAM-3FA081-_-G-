import enums.Gender;
import interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import enums.KindOfMeter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;
import interfaces.CrudCustomer;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CrudReadingTest {

    private CrudReading crudReading;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    public void setUp() throws SQLException {
        // Mocking the Connection, PreparedStatement, and ResultSet
        mockConnection = Mockito.mock(Connection.class);
        mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        mockResultSet = Mockito.mock(ResultSet.class);
        crudReading = new CrudReading();
        MockitoAnnotations.openMocks(this);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Set the mock connection to the DatabaseConnection singleton
        DatabaseConnection.getInstance().connection = mockConnection;
    }

    @Test
    public void testUpdateReadingById() throws SQLException {
        // Arrange
        Reading reading = new Reading(UUID.randomUUID(), "Updated comment", new Customer(UUID.randomUUID(), "Jane", "Doe", LocalDate.of(1990, 8, 15), Gender.M), LocalDate.now(), KindOfMeter.WASSER, 200.0, "updatedMeterId", true);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Mock 1 row affected

        // Act
        crudReading.updateReadingById(reading);

        // Assert
        verify(mockPreparedStatement, times(1)).setString(1, reading.getComment());
        verify(mockPreparedStatement, times(1)).setString(2, reading.getCustomer().getId().toString());
        verify(mockPreparedStatement, times(1)).setDate(3, java.sql.Date.valueOf(reading.getDateOfReading()));
        verify(mockPreparedStatement, times(1)).setString(4, reading.getKindOfMeter().name());
        verify(mockPreparedStatement, times(1)).setDouble(5, reading.getMeterCount());
        verify(mockPreparedStatement, times(1)).setString(6, reading.getMeterId());
        verify(mockPreparedStatement, times(1)).setBoolean(7, reading.getSubstitute());
        verify(mockPreparedStatement, times(1)).setObject(8, reading.getId());
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testDeleteReadingById() throws SQLException {
        // Arrange
        UUID readingId = UUID.randomUUID();
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Mock 1 row affected

        // Act
        crudReading.deleteReadingById(readingId);

        // Assert
        verify(mockPreparedStatement, times(1)).setObject(1, readingId);
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }


    @Test
    public void testReadReading() throws SQLException {
        // Arrange
        UUID readingId = UUID.randomUUID();
        String comment = "Test comment";
        String cust_id = UUID.randomUUID().toString();
        String date_of_reading = LocalDate.now().toString();
        String kind_of_meter = "WASSER";
        String meter_count = "200.0";
        String meter_id = "meter123";
        String substitute = "false";

        // Mocking the ResultSet to return specific values
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("comment")).thenReturn(comment);
        when(mockResultSet.getString("customer_id")).thenReturn(cust_id);
        when(mockResultSet.getString("date_of_reading")).thenReturn(date_of_reading);
        when(mockResultSet.getString("kind_of_meter")).thenReturn(kind_of_meter);
        when(mockResultSet.getString("meter_count")).thenReturn(meter_count);
        when(mockResultSet.getString("meter_id")).thenReturn(meter_id);
        when(mockResultSet.getString("substitute")).thenReturn(substitute);

        // Mocking the statement to return the prepared result set
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        Reading result = crudReading.readReading(readingId);

        // Assert
        assertNotNull(result);  // Assert that the result is not null
        assertEquals(comment, result.getComment());
        assertEquals(readingId, result.getId());
        assertEquals(kind_of_meter, result.getKindOfMeter().name());
    }

}




