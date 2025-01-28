package hausfix.entities;
import hausfix.enums.Gender;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

class CustomerTest {

    // Test für setFirstName() und getFirstName()
    @Test
    void testSetFirstNameAndGetFirstName() {
        Customer customer = new Customer();
        customer.setFirstName("John");
        assertEquals("John", customer.getFirstName(), "First name should be set and retrieved correctly.");
    }

    // Test für setLastName() und getLastName()
    @Test
    void testSetLastNameAndGetLastName() {
        Customer customer = new Customer();
        customer.setLastName("Doe");
        assertEquals("Doe", customer.getLastName(), "Last name should be set and retrieved correctly.");
    }

    // Test für setBirthDate() und getBirthDate()
    @Test
    void testSetBirthDateAndGetBirthDate() {
        Customer customer = new Customer();
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        customer.setBirthDate(birthDate);
        assertEquals(birthDate, customer.getBirthDate(), "Birthdate should be set and retrieved correctly.");
    }

    // Test für setGender() und getGender()
    @Test
    void testSetGenderAndGetGender() {
        Customer customer = new Customer();
        Gender gender = Gender.M; // Beispielwert für Gender (assumes Gender enum exists)
        customer.setGender(gender);
        assertEquals(gender, customer.getGender(), "Gender should be set and retrieved correctly.");
    }

    // Test für getId() und setId()
    @Test
    void testSetIdAndGetId() {
        Customer customer = new Customer();
        UUID uuid = UUID.randomUUID();
        customer.setId(uuid);
        assertEquals(uuid, customer.getId(), "ID should be set and retrieved correctly.");
    }
}
