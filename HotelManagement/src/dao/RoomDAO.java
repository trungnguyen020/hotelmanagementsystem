package dao;

import model.RoomView;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

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