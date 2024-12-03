package hausfix.entities;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import hausfix.enums.Gender;
import hausfix.interfaces.ICustomer;
import java.time.LocalDate;
import java.util.UUID;

public class Customer implements ICustomer {

    private UUID id;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private Gender gender;

    public Customer() {
    }

    public Customer(UUID id, String firstName, String lastName, LocalDate birthday, Gender gender) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.gender = gender;
    }



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

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public LocalDate getBirthDate() {
        return birthday;
    }

    @Override
    public Gender getGender() {
        return gender;
    }


    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;

    }
}
