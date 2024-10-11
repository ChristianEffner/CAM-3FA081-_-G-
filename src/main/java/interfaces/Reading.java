package interfaces;

import enums.KindOfMeter;

import java.time.LocalDate;

public class Reading implements IReading{

    private String comment;
    private ICustomer customer;
    private LocalDate dateOfReading;
    private KindOfMeter kindOfMeter;
    private Double meterCount;
    private String meterID;
    private Boolean substitude;


    @Override
    public void setComment(String comment) {

    }

    @Override
    public void setCustomer(ICustomer customer) {

    }

    @Override
    public void setDateOfReading(LocalDate dateOfReading) {

    }

    @Override
    public void setKindOfMeter(KindOfMeter kindOfMeter) {

    }

    @Override
    public void setMeterCount(Double meterCount) {

    }

    @Override
    public void setMeterId(String meterId) {

    }

    @Override
    public void setSubstitute(Boolean substitute) {

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
    public String printDateOfReading() {
        return null;
    }
}
