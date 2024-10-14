package interfaces;
import enums.Gender;
import java.time.LocalDate;

public class Costumer implements ICustomer {

    private ID id;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private Gender gender;

    public Costumer(ID id, String firstName, String lastName,LocalDate birthday, Gender gender) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.gender = gender;
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
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


}
