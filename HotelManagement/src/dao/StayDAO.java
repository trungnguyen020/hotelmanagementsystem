package dao;

import model.StayView;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StayDAO {

    // Check-in: tạo stay + đổi phòng sang OCCUPIED
    public int checkin(int customerId, int roomId, LocalDateTime checkinAt,
                      LocalDateTime expectedCheckoutAt, int createdBy) throws SQLException {

        String sqlStay =
            "INSERT INTO stays(customer_id, room_id, checkin_at, expected_checkout_at, status_id, created_by) " +
            "SELECT ?, ?, ?, ?, ss.id, ? " +
            "FROM stay_status ss WHERE ss.code='CHECKED_IN'";

        String sqlRoom =
            "UPDATE rooms r " +
            "JOIN room_status rs ON rs.code='OCCUPIED' " +
            "SET r.status_id = rs.id " +
            "WHERE r.id = ?";

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int stayId;

                try (PreparedStatement ps = c.prepareStatement(sqlStay, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, customerId);
                    ps.setInt(2, roomId);
                    ps.setTimestamp(3, Timestamp.valueOf(checkinAt));
                    ps.setTimestamp(4, Timestamp.valueOf(expectedCheckoutAt));
                    ps.setInt(5, createdBy);
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("Cannot get stay id");
                        stayId = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = c.prepareStatement(sqlRoom)) {
                    ps.setInt(1, roomId);
                    ps.executeUpdate();
                }

                c.commit();
                return stayId;
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public List<StayView> findCheckedInStays() throws SQLException {
        String sql =
            "SELECT s.id AS stay_id, r.id AS room_id, r.room_number, c.full_name AS customer_name, " +
            "       s.checkin_at, s.expected_checkout_at, rt.price_per_night " +
            "FROM stays s " +
            "JOIN stay_status ss ON ss.id = s.status_id " +
            "JOIN rooms r ON r.id = s.room_id " +
            "JOIN room_types rt ON rt.id = r.room_type_id " +
            "JOIN customers c ON c.id = s.customer_id " +
            "WHERE ss.code='CHECKED_IN' " +
            "ORDER BY s.checkin_at DESC";

        List<StayView> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StayView v = new StayView();
                v.setStayId(rs.getInt("stay_id"));
                v.setRoomId(rs.getInt("room_id"));
                v.setRoomNumber(rs.getString("room_number"));
                v.setCustomerName(rs.getString("customer_name"));
                v.setCheckinAt(rs.getTimestamp("checkin_at").toLocalDateTime());
                Timestamp t = rs.getTimestamp("expected_checkout_at");
                v.setExpectedCheckoutAt(t == null ? null : t.toLocalDateTime());
                v.setPricePerNight(rs.getBigDecimal("price_per_night"));
                list.add(v);
            }
        }
        return list;
    }

    public void extendExpectedCheckout(int stayId, int extraDays) throws SQLException {
        String sql =
            "UPDATE stays " +
            "SET expected_checkout_at = DATE_ADD(expected_checkout_at, INTERVAL ? DAY) " +
            "WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, extraDays);
            ps.setInt(2, stayId);
            ps.executeUpdate();
        }
    }

    // Checkout: tạo invoice + update stay CHECKED_OUT + đổi phòng AVAILABLE
    public void checkout(int stayId, LocalDateTime checkoutAt,
                         BigDecimal subtotal, BigDecimal discountPercent,
                         BigDecimal discountAmount, BigDecimal total,
                         int paidBy) throws SQLException {

        String sqlUpdateStay =
            "UPDATE stays s " +
            "JOIN stay_status ss ON ss.code='CHECKED_OUT' " +
            "SET s.checkout_at=?, s.status_id=ss.id " +
            "WHERE s.id=?";

        String sqlRoomAvailable =
            "UPDATE rooms r " +
            "JOIN stays s ON s.room_id=r.id " +
            "JOIN room_status rs ON rs.code='AVAILABLE' " +
            "SET r.status_id=rs.id " +
            "WHERE s.id=?";

        String sqlInvoice =
            "INSERT INTO invoices(stay_id, paid_at, paid_by, subtotal, discount_percent, discount_amount, total) " +
            "VALUES(?,?,?,?,?,?,?)";

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(sqlUpdateStay)) {
                    ps.setTimestamp(1, Timestamp.valueOf(checkoutAt));
                    ps.setInt(2, stayId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.prepareStatement(sqlRoomAvailable)) {
                    ps.setInt(1, stayId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.prepareStatement(sqlInvoice)) {
                    ps.setInt(1, stayId);
                    ps.setTimestamp(2, Timestamp.valueOf(checkoutAt));
                    ps.setInt(3, paidBy);
                    ps.setBigDecimal(4, subtotal);
                    ps.setBigDecimal(5, discountPercent);
                    ps.setBigDecimal(6, discountAmount);
                    ps.setBigDecimal(7, total);
                    ps.executeUpdate();
                }

                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}