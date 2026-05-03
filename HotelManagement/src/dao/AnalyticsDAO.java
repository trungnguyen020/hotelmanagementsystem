package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsDAO {

    public Map<String, Integer> getRoomTypeUsage() {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT rt.name as type, COUNT(s.id) as usage_count " +
                     "FROM stays s " +
                     "JOIN rooms r ON s.room_id = r.id " +
                     "JOIN room_types rt ON r.room_type_id = rt.id " +
                     "GROUP BY rt.name";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("type"), rs.getInt("usage_count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<String, Double> getRevenueByRoomType() {
        Map<String, Double> map = new HashMap<>();
        String sql = "SELECT rt.name as type, SUM(i.total) as total_revenue " +
                     "FROM stays s " +
                     "JOIN rooms r ON s.room_id = r.id " +
                     "JOIN room_types rt ON r.room_type_id = rt.id " +
                     "JOIN invoices i ON i.stay_id = s.id " +
                     "GROUP BY rt.name";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("type"), rs.getDouble("total_revenue"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<String, Integer> getTopCustomers(int limit) {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT c.full_name, COUNT(s.id) as stay_count " +
                     "FROM stays s " +
                     "JOIN customers c ON s.customer_id = c.id " +
                     "GROUP BY c.id, c.full_name " +
                     "ORDER BY stay_count DESC LIMIT ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("full_name"), rs.getInt("stay_count"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
