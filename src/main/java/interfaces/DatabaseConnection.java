package interfaces;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class DatabaseConnection implements IDatabaseConnection {

    Connection connection;
    IDatabaseConnection dbConnection;
    private static DatabaseConnection INSTANCE;


    public static DatabaseConnection getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new DatabaseConnection();
        }

        return INSTANCE;
    }


    @Override
    public IDatabaseConnection openConnection(Properties properties) {
        String url = properties.getProperty(  "db.url");
        String user = properties.getProperty("db.user");
        String pw = properties.getProperty("db.pw");
        System.out.println(pw);

        try {
            connection = DriverManager.getConnection(url, user, pw);
        } catch (SQLException e) {
            System.out.println(e);
        }
        System.out.println("connection established");
        return dbConnection;
    }


    @Override
    public void createAllTables() {

        String createCustomerTableSQL = "CREATE TABLE IF NOT EXISTS Customer (" +
                "id UUID PRIMARY KEY," +
                "first_name VARCHAR(255) NOT NULL," +
                "last_name VARCHAR(255) NOT NULL," +
                "birth_date DATE NOT NULL," +
                "gender CHAR(1)," +
                "FOREIGN KEY (gender) REFERENCES Gender(gender));";


        String createReadingTableSQL = "CREATE TABLE IF NOT EXISTS Reading (" +
                "id UUID PRIMARY KEY," +
                "comment VARCHAR(255)," +
                "customer_id UUID," +
                "date_of_reading DATE," +
                "kind_of_meter VARCHAR(10)," +
                "meter_count DOUBLE," +
                "meter_id VARCHAR(255)," +
                "substitute BOOLEAN," +
                "FOREIGN KEY (customer_id) REFERENCES Customer(id)," +
                "FOREIGN KEY (kind_of_meter) REFERENCES KindOfMeter(kind_of_meter));";


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
