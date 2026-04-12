package dao;

import util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;

public class ServiceUsageDAO {

    public void addUsage(int stayId, int serviceId, int quantity, String note) throws SQLException {
        String sql = "INSERT INTO service_usages(stay_id, service_id, quantity, used_at, note) VALUES (?,?,?,NOW(),?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, stayId);
            ps.setInt(2, serviceId);
            ps.setInt(3, quantity);
            ps.setString(4, note);
            ps.executeUpdate();
        }
    }

    public BigDecimal sumServiceAmountByStay(int stayId) throws SQLException {
        String sql =
            "SELECT COALESCE(SUM(su.quantity * sv.unit_price), 0) AS total " +
            "FROM service_usages su " +
            "JOIN services sv ON sv.id = su.service_id " +
            "WHERE su.stay_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, stayId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal("total");
            }
        }
    }
}