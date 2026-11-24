import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL =
            "jdbc:oracle:thin:@localhost:1521:orcle";

    private static final String USERNAME = "KRISHNA";
    private static final String PASSWORD = "kisu1122";

    public static Connection getConnection() throws Exception {

        return DriverManager.getConnection(
                URL,
                USERNAME,
                PASSWORD
        );
    }

    public static void main(String[] args) {

        try {

            Connection con = getConnection();

            System.out.println("Database Connected Successfully!");

            con.close();

        } catch (Exception e) {

            System.out.println("Database Connection Failed!");

            e.printStackTrace();
        }
    }
}