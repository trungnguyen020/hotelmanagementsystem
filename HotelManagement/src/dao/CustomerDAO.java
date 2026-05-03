package dao;

import model.Customer;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerDAO {

    public Customer insert(Customer cst) throws SQLException {
        String sql = "INSERT INTO customers(full_name, phone, id_number) VALUES (?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cst.getFullName());
            ps.setString(2, cst.getPhone());
            ps.setString(3, cst.getIdNumber());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) cst.setId(rs.getInt(1));
            }
            return cst;
        }
    }

    // Search theo Tên / CCCD / Phone
    public List<Customer> search(String keyword) throws SQLException {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) return Collections.emptyList();

        String sql =
            "SELECT id, full_name, phone, id_number " +
            "FROM customers " +
            "WHERE full_name LIKE ? OR id_number LIKE ? OR phone LIKE ? " +
            "ORDER BY full_name " +
            "LIMIT 30";

        List<Customer> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            String like = "%" + kw + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Customer cs = new Customer();
                    cs.setId(rs.getInt("id"));
                    cs.setFullName(rs.getString("full_name"));
                    cs.setPhone(rs.getString("phone"));
                    cs.setIdNumber(rs.getString("id_number"));
                    list.add(cs);
                }
            }
        }
        return list;
    }

    public List<Customer> findPaginated(String keyword, int offset, int limit) throws SQLException {
        String kw = keyword == null ? "" : keyword.trim();
        String sql =
            "SELECT id, full_name, phone, id_number " +
            "FROM customers " +
            "WHERE full_name LIKE ? OR id_number LIKE ? OR phone LIKE ? " +
            "ORDER BY full_name " +
            "LIMIT ? OFFSET ?";

        List<Customer> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            String like = "%" + kw + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setInt(4, limit);
            ps.setInt(5, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Customer cs = new Customer();
                    cs.setId(rs.getInt("id"));
                    cs.setFullName(rs.getString("full_name"));
                    cs.setPhone(rs.getString("phone"));
                    cs.setIdNumber(rs.getString("id_number"));
                    list.add(cs);
                }
            }
        }
        return list;
    }

    public int countTotal(String keyword) throws SQLException {
        String kw = keyword == null ? "" : keyword.trim();
        String sql = "SELECT COUNT(*) FROM customers WHERE full_name LIKE ? OR id_number LIKE ? OR phone LIKE ?";
        
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String like = "%" + kw + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }
}