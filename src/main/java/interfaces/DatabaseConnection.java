package interfaces;
import java.util.Properties;

public class DatabaseConnection implements IDatabaseConnection {


    @Override
    public IDatabaseConnection openConnection(Properties properties) {
        return null;
    }

    @Override
    public void createAllTables() {



    }

    @Override
    public void truncateAllTables() {

    }

    @Override
    public void removeAllTables() {

    }

    @Override
    public void closeConnection() {

    }
}
