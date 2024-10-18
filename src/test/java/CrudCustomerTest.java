import interfaces.Costumer;
import interfaces.DatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import enums.Gender;
import interfaces.CrudCustomer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CrudCustomerTest {

    private CrudCustomer crudCustomer;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    public void setUp() throws SQLException {
        // Mocking the Connection, PreparedStatement, and ResultSet
        mockConnection = Mockito.mock(Connection.class);
        mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        mockResultSet = Mockito.mock(ResultSet.class);

        crudCustomer = new CrudCustomer();
        // Set the mock connection to the DatabaseConnection singleton
        DatabaseConnection.getInstance().connection = mockConnection;
    }

    @Test
    public void testAddNewCustomer() throws SQLException {
        // Arrange
        Costumer customer = new Costumer(UUID.randomUUID(), "John", "Doe", LocalDate.of(1980, 5, 20), Gender.M);

        // Mock the prepared statement execution
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        crudCustomer.addNewCustomer(customer);

        // Assert
        verify(mockPreparedStatement, times(1)).setString(1, customer.getId().toString());
        verify(mockPreparedStatement, times(1)).setString(2, customer.getFirstName());
        verify(mockPreparedStatement, times(1)).setString(3, customer.getLastName());
        verify(mockPreparedStatement, times(1)).setString(4, customer.getBirthDate().toString());
        verify(mockPreparedStatement, times(1)).setString(5, customer.getGender().toString());
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testReadCustomer() throws SQLException {
        // Arrange
        UUID customerId = UUID.randomUUID();
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("first_name")).thenReturn("John");
        when(mockResultSet.getString("last_name")).thenReturn("Doe");
        when(mockResultSet.getDate("birth_date")).thenReturn(java.sql.Date.valueOf("1980-05-20"));
        when(mockResultSet.getString("gender")).thenReturn("M");

        // Act
        Costumer customer = crudCustomer.readCustomer(customerId);

        // Assert
        assertNotNull(customer);
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals(LocalDate.of(1980, 5, 20), customer.getBirthDate());
        assertEquals(Gender.M, customer.getGender());
    }

    @Test
    public void testUpdateCustomerById() throws SQLException {
        // Arrange
        Costumer customer = new Costumer(UUID.randomUUID(), "Jane", "Doe", LocalDate.of(1990, 8, 15), Gender.M);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Mock 1 row affected

        // Act
        crudCustomer.updateCustomerById(customer);

        // Assert
        verify(mockPreparedStatement, times(1)).setString(1, customer.getFirstName());
        verify(mockPreparedStatement, times(1)).setString(2, customer.getLastName());
        verify(mockPreparedStatement, times(1)).setDate(3, java.sql.Date.valueOf(customer.getBirthDate()));
        verify(mockPreparedStatement, times(1)).setString(4, customer.getGender().toString());
        verify(mockPreparedStatement, times(1)).setString(5, customer.getId().toString());
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testDeleteCustomerById() throws SQLException {
        // Arrange
        UUID customerId = UUID.randomUUID();
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Mock 1 row affected

        // Act
        crudCustomer.deleteCustomerById(customerId);

        // Assert
        verify(mockPreparedStatement, times(1)).setObject(1, customerId);
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }
}
