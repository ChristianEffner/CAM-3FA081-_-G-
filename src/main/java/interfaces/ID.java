package interfaces;
import java.util.UUID;

public class ID implements IID{

    private UUID id;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }
}
