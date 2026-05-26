package dao;

import util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;

public class ServiceUsageDAO {

    public void addUsage(int stayId, int serviceId, int quantity, java.time.LocalDateTime usedAt, String note) throws SQLException {
        String sql = "INSERT INTO service_usages(stay_id, service_id, quantity, used_at, note) VALUES (?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, stayId);
            ps.setInt(2, serviceId);
            ps.setInt(3, quantity);
            ps.setTimestamp(4, Timestamp.valueOf(usedAt));
            ps.setString(5, note);
            ps.executeUpdate();
        }
    }

    public java.util.List<model.ServiceUsageView> getUsagesByStay(int stayId) throws SQLException {
        String sql = "SELECT su.id, sv.name, su.quantity, sv.unit_price, su.used_at, su.note " +
                     "FROM service_usages su JOIN services sv ON su.service_id = sv.id " +
                     "WHERE su.stay_id = ? ORDER BY su.used_at DESC";
        java.util.List<model.ServiceUsageView> list = new java.util.ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, stayId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.ServiceUsageView view = new model.ServiceUsageView();
                    view.setId(rs.getInt("id"));
                    view.setServiceName(rs.getString("name"));
                    view.setQuantity(rs.getInt("quantity"));
                    view.setUnitPrice(rs.getBigDecimal("unit_price"));
                    Timestamp t = rs.getTimestamp("used_at");
                    if (t != null) view.setUsedAt(t.toLocalDateTime());
                    view.setNote(rs.getString("note"));
                    list.add(view);
                }
            }
        }
        return list;
    }

    public java.util.List<model.ServiceUsageView> getUsagesForToday() throws SQLException {
        String sql = "SELECT su.id, sv.name, su.quantity, sv.unit_price, su.used_at, su.note, r.room_number " +
                     "FROM service_usages su " +
                     "JOIN services sv ON su.service_id = sv.id " +
                     "JOIN stays s ON s.id = su.stay_id " +
                     "JOIN rooms r ON r.id = s.room_id " +
                     "WHERE DATE(su.used_at) = CURDATE() " +
                     "ORDER BY su.used_at DESC";
        java.util.List<model.ServiceUsageView> list = new java.util.ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.ServiceUsageView view = new model.ServiceUsageView();
                view.setId(rs.getInt("id"));
                view.setServiceName(rs.getString("name"));
                view.setQuantity(rs.getInt("quantity"));
                view.setUnitPrice(rs.getBigDecimal("unit_price"));
                Timestamp t = rs.getTimestamp("used_at");
                if (t != null) view.setUsedAt(t.toLocalDateTime());
                view.setNote(rs.getString("note"));
                view.setRoomNumber(rs.getString("room_number"));
                list.add(view);
            }
        }
        return list;
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