// Customer.java
package hausfix.entities;

import hausfix.enums.Gender;
import hausfix.interfaces.ICustomer;
import hausfix.resourcen.LocalDateAdapter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;
import java.util.UUID;

@XmlRootElement(name = "customer")
@XmlAccessorType(XmlAccessType.FIELD)
public class Customer implements ICustomer {

    @XmlElement
    private UUID id;

    @XmlElement
    private String firstName;

    @XmlElement
    private String lastName;

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlElement(name = "birthDate")
    private LocalDate birthday;

    @XmlElement
    private Gender gender;

    @XmlElement
    private Long userId;

    // --- Konstruktoren ---
    public Customer() {}

    public Customer(UUID id,
                    String firstName,
                    String lastName,
                    LocalDate birthday,
                    Gender gender,
                    Long userId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.birthday  = birthday;
        this.gender    = gender;
        this.userId    = userId;
    }

    public Customer(UUID id,
                    String firstName,
                    String lastName,
                    LocalDate birthday,
                    Gender gender) {
        this(id, firstName, lastName, birthday, gender, null);
    }

    // --- ICustomer-Implementierung und Getter/Setter ---
    @Override public UUID getId()                    { return id; }
    @Override public void setId(UUID id)             { this.id = id; }

    @Override public String getFirstName()           { return firstName; }
    @Override public void setFirstName(String firstName) { this.firstName = firstName; }

    @Override public String getLastName()            { return lastName; }
    @Override public void setLastName(String lastName)   { this.lastName = lastName; }

    @Override public LocalDate getBirthDate()        { return birthday; }
    @Override public void setBirthDate(LocalDate birthDate) { this.birthday = birthDate; }

    @Override public Gender getGender()              { return gender; }
    @Override public void setGender(Gender gender)   { this.gender = gender; }

    public Long getUserId()                          { return userId; }
    public void setUserId(Long userId)               { this.userId = userId; }
}
