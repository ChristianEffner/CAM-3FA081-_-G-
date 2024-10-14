package interfaces;
import enums.KindOfMeter;
import java.time.LocalDate;

public interface IReading {

    void setComment(String comment);
    void setCustomer(ICustomer customer);
    void setDateOfReading(LocalDate dateOfReading);
    void setKindOfMeter(KindOfMeter kindOfMeter);
    void setMeterCount(Double meterCount);
    void setMeterId(String meterId);
    void setSubstitute(Boolean substitute);
    String getComment();
    ICustomer getCustomer();
    LocalDate getDateOfReading();
    KindOfMeter getKindOfMeter();
    Double getMeterCount();
    String getMeterId();
    Boolean getSubstitute();
    LocalDate printDateOfReading();

}









