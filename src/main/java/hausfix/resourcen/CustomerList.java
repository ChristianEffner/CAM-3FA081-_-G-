// CustomerList.java
package hausfix.resourcen;

import hausfix.entities.Customer;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name = "customers")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomerList {

    @JsonProperty("customers")
    @XmlElement(name = "customer")
    private List<Customer> customers;

    public CustomerList() {}
    public CustomerList(List<Customer> list) {
        this.customers = list;
    }

    public List<Customer> getCustomers() { return customers; }
    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}
