package hausfix.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import hausfix.enums.KindOfMeter;
import hausfix.interfaces.ICustomer;
import hausfix.interfaces.IReading;
import java.time.LocalDate;
import java.util.UUID;

public class Reading implements IReading {

    private UUID id;
    private String comment;

    @JsonDeserialize(as = Customer.class)
    private ICustomer customer;

    private LocalDate dateOfReading;
    private KindOfMeter kindOfMeter;
    private Double meterCount;
    private String meterID;
    private Boolean substitude;

    // Neues Feld für die User-ID
    private Long userId;

    /**
     * Konstruktor mit userId.
     */
    public Reading(UUID id, String comment, Customer customer, LocalDate dateOfReading,
                   KindOfMeter kindOfMeter, Double meterCount, String meterID, Boolean substitude, Long userId) {
        this.id = id;
        this.comment = comment;
        this.customer = customer;
        this.dateOfReading = dateOfReading;
        this.kindOfMeter = kindOfMeter;
        this.meterCount = meterCount;
        this.meterID = meterID;
        this.substitude = substitude;
        this.userId = userId;
    }

    /**
     * Alter Konstruktor ohne userId – setzt userId auf null.
     */
    public Reading(UUID id, String comment, Customer customer, LocalDate dateOfReading,
                   KindOfMeter kindOfMeter, Double meterCount, String meterID, Boolean substitude) {
        this(id, comment, customer, dateOfReading, kindOfMeter, meterCount, meterID, substitude, null);
    }

    /**
     * Standardkonstruktor (wichtig für Jackson).
     */
    public Reading() {
        // Default-Konstruktor
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public void setCustomer(ICustomer customer) {
        this.customer = customer;
    }

    @Override
    public void setDateOfReading(LocalDate dateOfReading) {
        this.dateOfReading = dateOfReading;
    }

    @Override
    public void setKindOfMeter(KindOfMeter kindOfMeter) {
        this.kindOfMeter = kindOfMeter;
    }

    @Override
    public void setMeterCount(Double meterCount) {
        this.meterCount = meterCount;
    }

    @Override
    public void setMeterId(String meterId) {
        this.meterID = meterId;
    }

    @Override
    public void setSubstitute(Boolean substitute) {
        this.substitude = substitute;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public ICustomer getCustomer() {
        return customer;
    }

    @Override
    public LocalDate getDateOfReading() {
        return dateOfReading;
    }

    @Override
    public KindOfMeter getKindOfMeter() {
        return kindOfMeter;
    }

    @Override
    public Double getMeterCount() {
        return meterCount;
    }

    @Override
    public String getMeterId() {
        return meterID;
    }

    @Override
    public Boolean getSubstitute() {
        return substitude;
    }

    @Override
    public LocalDate printDateOfReading() {
        System.out.println(dateOfReading);
        return null;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    // Neuer Getter und Setter für userId
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
