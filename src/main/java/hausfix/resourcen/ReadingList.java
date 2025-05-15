// ReadingList.java
package hausfix.resourcen;

import hausfix.entities.Reading;
import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name = "readings")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ Reading.class })
public class ReadingList {

    @XmlElement(name = "reading")
    private List<Reading> readings;

    public ReadingList() {}
    public ReadingList(List<Reading> readings) {
        this.readings = readings;
    }

    public List<Reading> getReadings() { return readings; }
    public void setReadings(List<Reading> readings) {
        this.readings = readings;
    }
}
