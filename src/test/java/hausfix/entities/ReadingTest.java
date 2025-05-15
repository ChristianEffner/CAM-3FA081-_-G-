package hausfix.entities;

import hausfix.enums.Gender;
import hausfix.enums.KindOfMeter;
import hausfix.interfaces.ICustomer;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ReadingTest {

    @Test
    void setCommentAndGetComment() {
        Reading reading = new Reading();
        reading.setComment("Test Comment");
        assertEquals("Test Comment", reading.getComment());
    }

    @Test
    void setDateOfReadingAndGetDateOfReading() {
        LocalDate testDate = LocalDate.of(2023, 1, 1);
        Reading reading = new Reading();
        reading.setDateOfReading(testDate);
        assertEquals(testDate, reading.getDateOfReading());
    }

    @Test
    void setKindOfMeterAndGetKindOfMeter() {
        Reading reading = new Reading();
        reading.setKindOfMeter(KindOfMeter.STROM);
        assertEquals(KindOfMeter.STROM, reading.getKindOfMeter());
    }

    @Test
    void setMeterCountAndGetMeterCount() {
        Reading reading = new Reading();
        reading.setMeterCount(123.45);
        assertEquals(123.45, reading.getMeterCount());
    }

    @Test
    void setMeterIdAndGetMeterId() {
        Reading reading = new Reading();
        reading.setMeterId("METER123");
        assertEquals("METER123", reading.getMeterId());
    }

    @Test
    void setSubstituteAndGetSubstitute() {
        Reading reading = new Reading();
        reading.setSubstitute(true);
        assertTrue(reading.getSubstitute());
    }

    @Test
    void setIdAndGetId() {
        UUID testId = UUID.randomUUID();
        Reading reading = new Reading();
        reading.setId(testId);
        assertEquals(testId, reading.getId());
    }

    @Test
    void testConstructor() {
        UUID id = UUID.randomUUID();
        String comment = "Initial Comment";
        Customer customer = new Customer();
        LocalDate dateOfReading = LocalDate.of(2023, 1, 1);
        KindOfMeter kindOfMeter = KindOfMeter.STROM;
        Double meterCount = 100.0;
        String meterID = "METER001";
        Boolean substitute = false;

        Reading reading = new Reading(id, comment, customer, dateOfReading, kindOfMeter, meterCount, meterID, substitute);

        assertEquals(id, reading.getId());
        assertEquals(comment, reading.getComment());
        assertEquals(customer, reading.getCustomer());
        assertEquals(dateOfReading, reading.getDateOfReading());
        assertEquals(kindOfMeter, reading.getKindOfMeter());
        assertEquals(meterCount, reading.getMeterCount());
        assertEquals(meterID, reading.getMeterId());
        assertEquals(substitute, reading.getSubstitute());
    }

    @Test
    void testConstructorAndGetters() {
        UUID id = UUID.randomUUID();
        String comment = "Testkommentar";
        Customer customer = new Customer(UUID.randomUUID(), "Max", "Muster", LocalDate.of(1990, 1, 1), Gender.M);
        LocalDate date = LocalDate.of(2024, 5, 15);
        KindOfMeter meter = KindOfMeter.WASSER;
        double count = 123.45;
        String meterId = "M001";
        boolean substitute = true;
        long userId = 1L;

        Reading reading = new Reading(id, comment, customer, date, meter, count, meterId, substitute, userId);

        assertEquals(id, reading.getId());
        assertEquals(comment, reading.getComment());
        assertEquals(customer, reading.getCustomer());
        assertEquals(date, reading.getDateOfReading());
        assertEquals(meter, reading.getKindOfMeter());
        assertEquals(count, reading.getMeterCount());
        assertEquals(meterId, reading.getMeterId());
        assertTrue(reading.getSubstitute());
        assertEquals(userId, reading.getUserId());
    }

    @Test
    void testSetterMethods() {
        Reading reading = new Reading();
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(UUID.randomUUID(), "Erika", "Musterfrau", LocalDate.of(1985, 1, 1), Gender.W);

        reading.setId(id);
        reading.setComment("Kommentar");
        reading.setCustomer(customer);
        reading.setDateOfReading(LocalDate.of(2023, 12, 24));
        reading.setKindOfMeter(KindOfMeter.STROM);
        reading.setMeterCount(456.78);
        reading.setMeterId("Z123");
        reading.setSubstitute(false);
        reading.setUserId(5L);

        assertEquals(id, reading.getId());
        assertEquals("Kommentar", reading.getComment());
        assertEquals(customer, reading.getCustomer());
        assertEquals(LocalDate.of(2023, 12, 24), reading.getDateOfReading());
        assertEquals(KindOfMeter.STROM, reading.getKindOfMeter());
        assertEquals(456.78, reading.getMeterCount());
        assertEquals("Z123", reading.getMeterId());
        assertFalse(reading.getSubstitute());
        assertEquals(5L, reading.getUserId());
    }

    @Test
    void testPrintDateOfReading() {
        LocalDate expected = LocalDate.of(2024, 4, 4);
        Reading reading = new Reading();
        reading.setDateOfReading(expected);

        LocalDate result = reading.printDateOfReading();
        assertEquals(expected, result);
    }

}
