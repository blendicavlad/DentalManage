package DB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



public class DBConnect {

    private static final String DRIVER    = "com.mysql.cj.jdbc.Driver";
    private static final String URL       = "jdbc:mysql://localhost:3306/sys?";
    private static final String USER      = "root";
    private static final String PASSWORD  = "cataclysm91";
    private static final String UNICODE   = "autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Bucharest";
    private static Connection conn = null;

    static void initDB(){
        try {
            Class.forName(DRIVER).newInstance();
            conn = DriverManager.getConnection(URL+UNICODE, USER, PASSWORD);
        } catch (Exception ex) {
            System.out.println("SQLException: " + ex.getMessage());
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static Connection getConnection() throws RuntimeException{
        try{
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL+UNICODE, USER, PASSWORD);
        }catch(Exception ex){
            System.out.println(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    //Connect to DB
    static void dbConnect() throws SQLException, ClassNotFoundException {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("No db driver found!");
            e.printStackTrace();
            throw e;
        }
        System.out.println("Oracle JDBC Driver Registered!");
        try {
            conn = DriverManager.getConnection(URL + UNICODE, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console" + e);
            e.printStackTrace();
            throw e;
        }
    }

    public static void dbDisconnect() throws SQLException {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Disconnect failed! Check output console" + e);
        }
    }
}
