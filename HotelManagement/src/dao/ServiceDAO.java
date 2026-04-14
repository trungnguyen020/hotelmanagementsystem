package dao;

import model.Service;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    public List<Service> findActive() throws SQLException {
        String sql = "SELECT id, name, unit_price, unit, active FROM services WHERE active=TRUE ORDER BY name";
        List<Service> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Service s = new Service();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setUnitPrice(rs.getBigDecimal("unit_price"));
                s.setUnit(rs.getString("unit"));
                s.setActive(rs.getBoolean("active"));
                list.add(s);
            }
        }
        return list;
    }

    public List<Service> findAllAdmin() throws SQLException {
        String sql = "SELECT id, name, unit_price, unit, active FROM services ORDER BY name";
        List<Service> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Service s = new Service();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setUnitPrice(rs.getBigDecimal("unit_price"));
                s.setUnit(rs.getString("unit"));
                s.setActive(rs.getBoolean("active"));
                list.add(s);
            }
        }
        return list;
    }

    public void insert(Service s) throws SQLException {
        String sql = "INSERT INTO services(name, unit_price, unit, active) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setBigDecimal(2, s.getUnitPrice());
            ps.setString(3, s.getUnit());
            ps.setBoolean(4, s.isActive());
            ps.executeUpdate();
        }
    }

    public void update(Service s) throws SQLException {
        String sql = "UPDATE services SET name = ?, unit_price = ?, unit = ?, active = ? WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setBigDecimal(2, s.getUnitPrice());
            ps.setString(3, s.getUnit());
            ps.setBoolean(4, s.isActive());
            ps.setInt(5, s.getId());
            ps.executeUpdate();
        }
    }

    public void deleteOrDeactivate(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // Try to delete physically
                String sqlDel = "DELETE FROM services WHERE id = ?";
                try (PreparedStatement ps = c.prepareStatement(sqlDel)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                // Fallback: set active = false
                String sqlUpd = "UPDATE services SET active = FALSE WHERE id = ?";
                try (PreparedStatement ps = c.prepareStatement(sqlUpd)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                c.commit();
                throw new SQLException("Dịch vụ này đã được sử dụng nên không thể xóa, hệ thống đã chuyển trạng thái thành Ngừng kinh doanh (Inactive).");
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}