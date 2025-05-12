package hausfix.entities;

import hausfix.enums.Gender;
import hausfix.interfaces.ICustomer;
import hausfix.resourcen.LocalDateAdapter;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;
import java.util.UUID;

@XmlRootElement(name = "customer")
public class Customer implements ICustomer {

    private UUID id;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private Gender gender;

    // NEU: Welcher User besitzt diesen Customer?
    private Long userId;

    public Customer(UUID id, String firstName, String lastName, LocalDate birthday, Gender gender) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.gender = gender;
    }

    public Customer() {}

    public Customer(UUID id, String firstName, String lastName, LocalDate birthday, Gender gender, Long userId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.gender = gender;
        this.userId = userId;
    }

    // Getter/Setter für userId
    @XmlElement(name = "userId")
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // Implementierung des ICustomer-Interfaces & andere Felder
    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public void setLastName(String lastname) {
        this.lastName = lastname;
    }

    @Override
    public void setBirthDate(LocalDate birthDate) {
        this.birthday = birthDate;
    }

    @Override
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    @XmlElement(name = "firstName")
    @Override
    public String getFirstName() {
        return firstName;
    }
    @XmlElement(name = "lastName")
    @Override
    public String getLastName() {
        return lastName;
    }
    @XmlElement(name = "birthDate")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @Override
    public LocalDate getBirthDate() {
        return birthday;
    }
    @XmlElement(name = "gender")
    @Override
    public Gender getGender() {
        return gender;
    }
    @XmlElement(name = "id")
    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }
}
