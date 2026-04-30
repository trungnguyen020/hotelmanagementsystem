package dao;

import model.DiscountRule;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DiscountRuleDAO {

    public List<DiscountRule> findAll() throws SQLException {
        String sql = "SELECT * FROM discount_rules ORDER BY min_days DESC";
        List<DiscountRule> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DiscountRule rule = new DiscountRule();
                rule.setId(rs.getInt("id"));
                rule.setMinDays(rs.getInt("min_days"));
                rule.setDiscountPercent(rs.getBigDecimal("discount_percent"));
                list.add(rule);
            }
        }
        return list;
    }

    public void insert(DiscountRule rule) throws SQLException {
        String sql = "INSERT INTO discount_rules(min_days, discount_percent) VALUES (?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, rule.getMinDays());
            ps.setBigDecimal(2, rule.getDiscountPercent());
            ps.executeUpdate();
        }
    }

    public void update(DiscountRule rule) throws SQLException {
        String sql = "UPDATE discount_rules SET min_days = ?, discount_percent = ? WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, rule.getMinDays());
            ps.setBigDecimal(2, rule.getDiscountPercent());
            ps.setInt(3, rule.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM discount_rules WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
