package hausfix.interfaces;
import java.sql.Connection;

import java.util.Properties;


public interface IDatabaseConnection {

    Connection openConnection(Properties properties);

    void createAllTables();

    void truncateAllTables();

    void removeAllTables();

    void closeConnection();

}
