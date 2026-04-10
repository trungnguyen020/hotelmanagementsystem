package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String URL =
        "jdbc:mysql://localhost:3306/hotel_mgmt_v2?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "123456"; // đổi theo máy bạn

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "Chưa add MySQL Connector/J vào Build Path (mysql-connector-j-8.x.x.jar)", e
            );
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}