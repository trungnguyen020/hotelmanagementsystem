package app;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBTest {
    public static void main(String[] args) {
        String sql = "SELECT COUNT(*) AS cnt FROM room_types";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            int count = rs.getInt("cnt");
            System.out.println("OK: Connected. room_types count = " + count);

        } catch (Exception e) {
            System.err.println("FAILED: DB connection/query error");
            e.printStackTrace();
        }
    }
}