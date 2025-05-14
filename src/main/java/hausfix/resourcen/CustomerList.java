package hausfix.resourcen;

import com.fasterxml.jackson.annotation.JsonProperty;
import hausfix.entities.Customer;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "customers")
public class CustomerList {
    private List<Customer> customers;

    @JsonProperty("customers")
    @XmlElement(name = "customer")
    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public CustomerList() {}

    public CustomerList(List<Customer> customers) {
        this.customers = customers;
    }
}
