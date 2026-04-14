package dao;

import model.RoomType;
import model.RoomView;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public List<RoomType> findAllRoomTypes() throws SQLException {
        String sql = "SELECT id, name, price_per_night, capacity, description FROM room_types ORDER BY name";
        List<RoomType> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RoomType t = new RoomType();
                t.setId(rs.getInt("id"));
                t.setName(rs.getString("name"));
                t.setPricePerNight(rs.getBigDecimal("price_per_night"));
                t.setCapacity(rs.getInt("capacity"));
                t.setDescription(rs.getString("description"));
                list.add(t);
            }
        }
        return list;
    }

    public List<RoomView> findAll() throws SQLException {
        String sql =
            "SELECT r.id AS room_id, r.room_number, rt.name AS room_type, rt.price_per_night, rs.code AS status_code " +
            "FROM rooms r " +
            "JOIN room_types rt ON rt.id = r.room_type_id " +
            "JOIN room_status rs ON rs.id = r.status_id " +
            "ORDER BY r.room_number";

        List<RoomView> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RoomView v = new RoomView();
                v.setRoomId(rs.getInt("room_id"));
                v.setRoomNumber(rs.getString("room_number"));
                v.setRoomType(rs.getString("room_type"));
                v.setPricePerNight(rs.getBigDecimal("price_per_night"));
                v.setStatus(rs.getString("status_code"));
                list.add(v);
            }
        }
        return list;
    }

    public void insert(String roomNumber, int roomTypeId, String statusCode, String note) throws SQLException {
        String sql = "INSERT INTO rooms(room_number, room_type_id, status_id, note) " +
                     "SELECT ?, ?, id, ? FROM room_status WHERE code = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            ps.setInt(2, roomTypeId);
            ps.setString(3, note != null ? note : "");
            ps.setString(4, statusCode);
            ps.executeUpdate();
        }
    }

    public void update(int roomId, String roomNumber, int roomTypeId, String statusCode, String note) throws SQLException {
        String sql = "UPDATE rooms r " +
                     "JOIN room_status rs ON rs.code = ? " +
                     "SET r.room_number = ?, r.room_type_id = ?, r.status_id = rs.id, r.note = ? " +
                     "WHERE r.id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, statusCode);
            ps.setString(2, roomNumber);
            ps.setInt(3, roomTypeId);
            ps.setString(4, note != null ? note : "");
            ps.setInt(5, roomId);
            ps.executeUpdate();
        }
    }

    public void deleteOrHide(int roomId) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // Try physical delete
                String sqlDel = "DELETE FROM rooms WHERE id = ?";
                try (PreparedStatement ps = c.prepareStatement(sqlDel)) {
                    ps.setInt(1, roomId);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                // If failed (due to foreign key like stays), update status to MAINTENANCE (3) or specific code.
                String sqlUpd = "UPDATE rooms r JOIN room_status rs ON rs.code = 'MAINTENANCE' SET r.status_id = rs.id WHERE r.id = ?";
                try (PreparedStatement ps = c.prepareStatement(sqlUpd)) {
                    ps.setInt(1, roomId);
                    ps.executeUpdate();
                }
                c.commit();
                throw new SQLException("Phòng đã từng có người ở nên không thể xóa, tự động đổi trạng thái thành BẢO TRÌ (MAINTENANCE).");
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public void updateStatusByCode(int roomId, String statusCode) throws SQLException {
        String sql =
            "UPDATE rooms r " +
            "JOIN room_status rs ON rs.code = ? " +
            "SET r.status_id = rs.id " +
            "WHERE r.id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, statusCode);
            ps.setInt(2, roomId);
            ps.executeUpdate();
        }
    }
}