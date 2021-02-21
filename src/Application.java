import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;

public class Application {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        Scanner sc = new Scanner(System.in);
        System.out.println("***   Newton Hotels   ***");
        System.out.println("*** Vänligen logga in ***");
        System.out.print("Ange användarnamn: ");
        String username = sc.nextLine();
        System.out.print("Ange lösenord: ");
        String password = sc.nextLine();
        int loginID=-1;
        int staffID=-1;
        StringBuilder md5Password = md5Pass(password);
        Connection conn = null;
        try {
            conn = SQLConnection();
            if (conn != null) {
                PreparedStatement pstmt = conn.prepareStatement("SELECT loginID, staffID FROM Hotels.StaffLogin WHERE username=? AND password=?");
                pstmt.setString(1, username);
                pstmt.setString(2, md5Password.toString());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                   loginID = rs.getInt("loginID");
                   staffID = rs.getInt("staffID");
                }
                pstmt = conn.prepareStatement("EXEC FetchStaffData @staffid=?");
                pstmt.setInt(1, staffID);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    System.out.println("Välkommen " + rs.getString("name") + ", " + rs.getString("pos"));
                    System.out.println("");
                }

            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        if (staffID>0) {
            while(true) {
                System.out.println("\n[1] Sök reservation [2] Sök lediga rum [0] Avsluta");
                String menu = sc.nextLine();
                if(menu.equals("1")) {
                    System.out.println("\nSök reservation");
                    System.out.print("Datum för reservationen som eftersöks (t.ex. 20210301): ");
                    String resDate = sc.nextLine();
                    System.out.print("Efternamn på reservationen: ");
                    String resName = sc.nextLine();

                    conn = null;
                    Statement stmt;
                    try {
                        conn = SQLConnection();
                        if (conn != null) {
                            stmt = conn.createStatement();
                            String sql;
                            sql = "EXEC GetReservation @date='"+resDate+"', @lname='"+resName+"'";
                            ResultSet rs = stmt.executeQuery(sql);
                            System.out.println("\nIncheckning \tUtcheckning \tRum \t\tGäst");
                            while (rs.next()) {
                                System.out.println((rs.getString("Incheckning")).substring(0,10) + " \t\t" + (rs.getString("Utcheckning")).substring(0,10) + " \t\t" + rs.getString("Rum") + " \t\t" + rs.getString("Gäst"));
                            }
                        }

                    } catch (SQLException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                    } finally {
                        try {
                            if (conn != null && !conn.isClosed()) {
                                conn.close();
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                if(menu.equals("2")) {
                    System.out.println("\nHitta lediga rum");
                    System.out.print("Incheckningsdatum (t.ex. 20210301): ");
                    String bookDate = sc.nextLine();
                    System.out.print("Antal nätter: ");
                    String bookNights = sc.nextLine();

                    try {
                        Statement stmt;
                        conn = SQLConnection();
                        if (conn != null) {
                            stmt = conn.createStatement();
                            String sql;
                            sql = "EXEC CheckAvailableRooms @from='" + bookDate + "', @nights=" + bookNights;
                            ResultSet rs = stmt.executeQuery(sql);
                            System.out.println("\nRum \tPris \t\tRumstyp");
                            while (rs.next()) {
                                System.out.println(rs.getString("Rum") + " \t" + rs.getInt("Pris") + ":- \t\t" + rs.getString("Rumstyp"));
                            }
                        }

                    } catch (SQLException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                    } finally {
                        try {
                            if (conn != null && !conn.isClosed()) {
                                conn.close();
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }

                }
                if(menu.equals("0")) {
                    break;
                }
            }
        }
    }

    public static Properties getConnectionData() {
        Properties props = new Properties();
        String fileName = "db.properties";
        try (FileInputStream in = new FileInputStream(fileName)) {
            props.load(in);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return props;
    }

    public static Connection SQLConnection() throws ClassNotFoundException, SQLException {
        Connection conn;
        Properties props = getConnectionData();
        String url = props.getProperty("db.url");
        String username = props.getProperty("db.user");
        String password = props.getProperty("db.pass");
        String myDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        Class.forName(myDriver);
        conn = DriverManager.getConnection(url, username, password);
        return conn;
    }

    /**
     * Builds a md5-hash of password
     * @author Michael
     * @param text input string
     * @return md5 password
     * @throws NoSuchAlgorithmException error output
     */
    private static StringBuilder md5Pass(String text) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(text.getBytes());
        byte[] md5Password = md5.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : md5Password) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb;
    }

}
