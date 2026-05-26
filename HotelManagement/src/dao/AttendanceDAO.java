package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AttendanceDAO {

    public boolean hasCheckedInToday(int employeeId) throws SQLException {
        String sql = "SELECT id FROM attendance WHERE employee_id = ? AND date = CURDATE()";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void checkIn(int employeeId) throws SQLException {
        if (hasCheckedInToday(employeeId)) return;
        String sql = "INSERT INTO attendance (employee_id, check_in_time, date) VALUES (?, NOW(), CURDATE())";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.executeUpdate();
        }
    }

    public int countWorkedDays(int employeeId, int month, int year) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT date) FROM attendance WHERE employee_id = ? AND MONTH(date) = ? AND YEAR(date) = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }
}
