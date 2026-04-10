package dao;

import model.Employee;
import model.Role;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}