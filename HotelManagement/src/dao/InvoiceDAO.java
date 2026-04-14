package dao;

import util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    public static class InvoiceView {
        public int id;
        public int stayId;
        public LocalDateTime paidAt;
        public String paidByName;
        public BigDecimal subtotal;
        public BigDecimal discountAmount;
        public BigDecimal total;
    }

    public List<InvoiceView> getAllInvoices() throws SQLException {
        return getFilteredInvoices(null, null, null);
    }

    public List<InvoiceView> getFilteredInvoices(Integer day, Integer month, Integer year) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT i.id, i.stay_id, i.paid_at, e.full_name AS paid_by_name, i.subtotal, i.discount_amount, i.total " +
            "FROM invoices i " +
            "LEFT JOIN employees e ON i.paid_by = e.id " +
            "WHERE 1=1 "
        );

        if (day != null && day > 0) sql.append(" AND DAY(i.paid_at) = ?");
        if (month != null && month > 0) sql.append(" AND MONTH(i.paid_at) = ?");
        if (year != null && year > 0) sql.append(" AND YEAR(i.paid_at) = ?");
        
        sql.append(" ORDER BY i.paid_at DESC");

        List<InvoiceView> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            
            int pIndex = 1;
            if (day != null && day > 0) ps.setInt(pIndex++, day);
            if (month != null && month > 0) ps.setInt(pIndex++, month);
            if (year != null && year > 0) ps.setInt(pIndex++, year);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceView v = new InvoiceView();
                    v.id = rs.getInt("id");
                    v.stayId = rs.getInt("stay_id");
                    v.paidAt = rs.getTimestamp("paid_at").toLocalDateTime();
                    v.paidByName = rs.getString("paid_by_name");
                    v.subtotal = rs.getBigDecimal("subtotal");
                    v.discountAmount = rs.getBigDecimal("discount_amount");
                    v.total = rs.getBigDecimal("total");
                    list.add(v);
                }
            }
        }
        return list;
    }
}
