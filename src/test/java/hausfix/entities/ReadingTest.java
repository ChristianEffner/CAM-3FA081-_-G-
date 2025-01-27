package hausfix.entities;

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
    void setCustomerAndGetCustomer() {
        ICustomer mockCustomer = mock(ICustomer.class);
        Reading reading = new Reading();
        reading.setCustomer(mockCustomer);
        assertEquals(mockCustomer, reading.getCustomer());
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
    void printDateOfReading() {
        LocalDate testDate = LocalDate.of(2023, 1, 1);
        Reading reading = new Reading();
        reading.setDateOfReading(testDate);

        LocalDate printedDate = reading.printDateOfReading();
        assertNull(printedDate, "Die Methode printDateOfReading() gibt immer null zurück.");
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
}
