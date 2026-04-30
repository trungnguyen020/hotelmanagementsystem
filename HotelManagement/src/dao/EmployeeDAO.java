package dao;

import model.Employee;
import model.Role;
import util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public Employee login(String username, String password) throws SQLException {
        String sql = "SELECT e.id, e.username, e.password_hash, e.full_name, r.code AS role_code " +
                "FROM employees e " +
                "JOIN roles r ON r.id = e.role_id " +
                "JOIN employee_status es ON es.id = e.status_id " +
                "WHERE e.username = ? AND es.code = 'ACTIVE'";

        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;
                
                String hashedPw = rs.getString("password_hash");
                // Fallback for plain-text or BCrypt check
                boolean pwMatch = false;
                if (hashedPw != null && hashedPw.startsWith("$2a$")) {
                    pwMatch = BCrypt.checkpw(password, hashedPw);
                } else {
                    pwMatch = password.equals(hashedPw);
                }

                if (!pwMatch) {
                    return null;
                }

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

    public List<Employee> findPaginatedAdmin(String keyword, int offset, int limit) throws SQLException {
        String sql = "SELECT e.id, e.username, e.full_name, r.code AS role_code, es.code AS status_code " +
                "FROM employees e " +
                "JOIN roles r ON r.id = e.role_id " +
                "JOIN employee_status es ON es.id = e.status_id " +
                "WHERE e.username LIKE ? OR e.full_name LIKE ? " +
                "ORDER BY e.created_at DESC LIMIT ? OFFSET ?";

        List<Employee> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            
            String kw = "%" + (keyword == null ? "" : keyword) + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setInt(3, limit);
            ps.setInt(4, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Employee e = new Employee();
                    e.setId(rs.getInt("id"));
                    e.setUsername(rs.getString("username"));
                    e.setFullName(rs.getString("full_name"));
                    String roleCode = rs.getString("role_code");
                    e.setRole(Role.valueOf(roleCode));
                    e.setStatus(rs.getString("status_code"));
                    list.add(e);
                }
            }
        }
        return list;
    }

    public int countTotalAdmin(String keyword) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees e " +
                "WHERE e.username LIKE ? OR e.full_name LIKE ?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            String kw = "%" + (keyword == null ? "" : keyword) + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public void insert(Employee e, String plainPassword) throws SQLException {
        String sql = "INSERT INTO employees(username, password_hash, full_name, role_id, status_id) VALUES (?, ?, ?, ?, 1)";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getUsername());
            ps.setString(2, BCrypt.hashpw(plainPassword, BCrypt.gensalt())); 
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
                ps.setString(idx++, BCrypt.hashpw(newPlainPassword, BCrypt.gensalt()));
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

    public void deactivate(int id) throws SQLException {
        String sql = "UPDATE employees SET status_id = 2 WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void activate(int id) throws SQLException {
        String sql = "UPDATE employees SET status_id = 1 WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}