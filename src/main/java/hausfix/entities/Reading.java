// Reading.java
package hausfix.entities;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import hausfix.enums.KindOfMeter;
import hausfix.interfaces.ICustomer;
import hausfix.interfaces.IReading;
import hausfix.resourcen.LocalDateAdapter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;
import java.util.UUID;

@XmlRootElement(name = "reading")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ Customer.class })
public class Reading implements IReading {

    @XmlElement
    private UUID id;

    @XmlElement
    private String comment;

    @JsonDeserialize(as = Customer.class)
    @XmlElement(type = Customer.class)
    private Customer customer;

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlElement(name = "dateOfReading")
    private LocalDate dateOfReading;

    @XmlElement
    private KindOfMeter kindOfMeter;

    @XmlElement
    private Double meterCount;

    @XmlElement(name = "meterId")
    private String meterID;

    /** XML-Tag heißt weiterhin <substitude>,
     *  JSON-Property heißt "substitute" */
    @XmlElement(name = "substitude")
    @JsonProperty("substitute")
    @JsonAlias("substitude")
    private Boolean substitute;

    @XmlElement
    private Long userId;

    // --- Konstruktoren ---
    public Reading() {}

    public Reading(UUID id,
                   String comment,
                   Customer customer,
                   LocalDate dateOfReading,
                   KindOfMeter kindOfMeter,
                   Double meterCount,
                   String meterID,
                   Boolean substitute,
                   Long userId) {
        this.id             = id;
        this.comment        = comment;
        this.customer       = customer;
        this.dateOfReading  = dateOfReading;
        this.kindOfMeter    = kindOfMeter;
        this.meterCount     = meterCount;
        this.meterID        = meterID;
        this.substitute     = substitute;
        this.userId         = userId;
    }

    public Reading(UUID id,
                   String comment,
                   Customer customer,
                   LocalDate dateOfReading,
                   KindOfMeter kindOfMeter,
                   Double meterCount,
                   String meterID,
                   Boolean substitute) {
        this(id, comment, customer, dateOfReading, kindOfMeter, meterCount, meterID, substitute, null);
    }

    // --- IReading-Implementierung und Getter/Setter ---
    @Override public UUID getId()                           { return id; }
    @Override public void setId(UUID id)                    { this.id = id; }

    @Override public String getComment()                    { return comment; }
    @Override public void setComment(String comment)        { this.comment = comment; }

    @Override public Customer getCustomer()                 { return customer; }
    @Override public void setCustomer(ICustomer customer) {
        if (!(customer instanceof Customer))
            throw new IllegalArgumentException("Unsupported ICustomer implementation");
        this.customer = (Customer) customer;
    }

    @Override public LocalDate getDateOfReading()           { return dateOfReading; }
    @Override public void setDateOfReading(LocalDate dateOfReading) {
        this.dateOfReading = dateOfReading;
    }

    @Override public KindOfMeter getKindOfMeter()           { return kindOfMeter; }
    @Override public void setKindOfMeter(KindOfMeter kindOfMeter) {
        this.kindOfMeter = kindOfMeter;
    }

    @Override public Double getMeterCount()                 { return meterCount; }
    @Override public void setMeterCount(Double meterCount)  { this.meterCount = meterCount; }

    @Override public String getMeterId()                    { return meterID; }
    @Override public void setMeterId(String meterID)        { this.meterID = meterID; }

    @Override public Boolean getSubstitute()                { return substitute; }
    @Override public void setSubstitute(Boolean substitute) { this.substitute = substitute; }

    public Long getUserId()                                 { return userId; }
    public void setUserId(Long userId)                      { this.userId = userId; }

    @Override
    public LocalDate printDateOfReading() {
        System.out.println(dateOfReading);
        return dateOfReading;
    }
}
