package interfaces;
import enums.Gender;
import java.time.LocalDate;

public class Costumer implements ICustomer {

    private String firstName;
    private String lastName;
    private LocalDate birthday;



    @Override
    public void setFirstName(String firstName) {

    }

    @Override
    public void setLastName(String lastname) {

    }

    @Override
    public void setBirthDate(LocalDate birthDate) {

    }

    @Override
    public void setGender(Gender gender) {

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
        return null;
    }
}
