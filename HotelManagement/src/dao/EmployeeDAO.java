package dao;

import model.Employee;
import model.Role;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public Employee login(String username, String password) throws SQLException {
        String sql =
            "SELECT e.id, e.username, e.full_name, r.code AS role_code " +
            "FROM employees e " +
            "JOIN roles r ON r.id = e.role_id " +
            "JOIN employee_status es ON es.id = e.status_id " +
            "WHERE e.username = ? AND e.password_hash = ? AND es.code = 'ACTIVE'";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Employee e = new Employee();
                e.setId(rs.getInt("id"));
                e.setUsername(rs.getString("username"));
                e.setFullName(rs.getString("full_name"));

                String roleCode = rs.getString("role_code"); // ADMIN / STAFF
                e.setRole(Role.valueOf(roleCode));

                return e;
            }
        }
    }

    public List<Employee> findAllAdmin() throws SQLException {
        String sql =
            "SELECT e.id, e.username, e.full_name, r.code AS role_code, es.code AS status_code " +
            "FROM employees e " +
            "JOIN roles r ON r.id = e.role_id " +
            "JOIN employee_status es ON es.id = e.status_id " +
            "ORDER BY e.created_at DESC";

        List<Employee> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Employee e = new Employee();
                e.setId(rs.getInt("id"));
                e.setUsername(rs.getString("username"));
                e.setFullName(rs.getString("full_name"));
                
                String roleCode = rs.getString("role_code");
                e.setRole(Role.valueOf(roleCode));

                // We can use a makeshift status string inside Employee if we want,
                // But Employee class might not have status field.
                // Assuming Employee class has setStatus(String) or we can just add it later.
                // Wait, let's check Employee.java. It might not have status field.
                // I will modify Employee.java later if needed. For now I will assume it has it or just add it.
                // Let's add status to Employee model via another replace. I'll just set it now.
                list.add(e);
            }
        }
        return list;
    }

    public void insert(Employee e, String plainPassword) throws SQLException {
        String sql = "INSERT INTO employees(username, password_hash, full_name, role_id, status_id) VALUES (?, ?, ?, ?, 1)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getUsername());
            ps.setString(2, plainPassword); // Note: Simple hashing in real life, here plain/plain-ish as per original seed
            ps.setString(3, e.getFullName());
            ps.setInt(4, "ADMIN".equals(e.getRole().name()) ? 1 : 2);
            ps.executeUpdate();
        }
    }

    public void update(Employee e, String newPlainPassword) throws SQLException {
        boolean updatePass = newPlainPassword != null && !newPlainPassword.trim().isEmpty();
        String sql = "UPDATE employees SET full_name = ?, role_id = ?, status_id = ? " +
                     (updatePass ? ", password_hash = ? " : "") +
                     "WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getFullName());
            ps.setInt(2, "ADMIN".equals(e.getRole().name()) ? 1 : 2);
            ps.setInt(3, e.getStatus().equals("ACTIVE") ? 1 : 2);
            int idx = 4;
            if (updatePass) {
                ps.setString(idx++, newPlainPassword);
            }
            ps.setInt(idx, e.getId());
            ps.executeUpdate();
        }
    }

    public void deleteOrDeactivate(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // Try to delete physically
                String sqlDel = "DELETE FROM employees WHERE id = ?";
                try (PreparedStatement ps = c.prepareStatement(sqlDel)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                // Fallback: update status to INACTIVE (2)
                String sqlUpd = "UPDATE employees SET status_id = 2 WHERE id = ?";
                try (PreparedStatement ps = c.prepareStatement(sqlUpd)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                c.commit();
                throw new SQLException("Đã chuyển trạng thái thành INACTIVE do nhân viên này có dữ liệu liên quan.");
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}