package dao;

import util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SalaryDAO {

    public static class SalaryReportRow {
        public int empId;
        public String username;
        public String fullName;
        public String role;
        public BigDecimal baseSalary;
        public BigDecimal revenueCheckin;
        public BigDecimal revenueCheckout;
        public BigDecimal commission;
        public BigDecimal totalSalary;
    }

    public List<SalaryReportRow> getSalaryReport(Integer month, Integer year) throws Exception {
        String cinSub = "SELECT s.created_by AS emp_id, SUM(i.total) AS total_checkin FROM invoices i JOIN stays s ON i.stay_id = s.id";
        String coutSub = "SELECT i.paid_by AS emp_id, SUM(i.total) AS total_checkout FROM invoices i";

        if (month != null && year != null) {
            cinSub += " WHERE MONTH(i.paid_at) = ? AND YEAR(i.paid_at) = ?";
            coutSub += " WHERE MONTH(i.paid_at) = ? AND YEAR(i.paid_at) = ?";
        }
        cinSub += " GROUP BY s.created_by";
        coutSub += " GROUP BY i.paid_by";

        String sql = "SELECT e.id, e.username, e.full_name, r.code AS role_code, " +
                "  COALESCE(cin.total_checkin, 0) AS revenue_checkin, " +
                "  COALESCE(cout.total_checkout, 0) AS revenue_checkout " +
                "FROM employees e " +
                "JOIN roles r ON e.role_id = r.id " +
                "LEFT JOIN (" + cinSub + ") cin ON cin.emp_id = e.id " +
                "LEFT JOIN (" + coutSub + ") cout ON cout.emp_id = e.id " +
                "WHERE r.code != 'ADMIN' " +
                "ORDER BY e.id";

        List<SalaryReportRow> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            if (month != null && year != null) {
                ps.setInt(1, month);
                ps.setInt(2, year);
                ps.setInt(3, month);
                ps.setInt(4, year);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SalaryReportRow row = new SalaryReportRow();
                    row.empId = rs.getInt("id");
                    row.username = rs.getString("username");
                    row.fullName = rs.getString("full_name");
                    row.role = rs.getString("role_code");
                    row.revenueCheckin = rs.getBigDecimal("revenue_checkin");
                    row.revenueCheckout = rs.getBigDecimal("revenue_checkout");

                    // Lương cơ bản của STAFF
                    row.baseSalary = new BigDecimal("7000000"); // 7.000.000 VNĐ

                    // Hoa hồng: 7% doanh thu Check-in, 3% doanh thu Check-out
                    BigDecimal commCheckin = row.revenueCheckin.multiply(new BigDecimal("0.07"));
                    BigDecimal commCheckout = row.revenueCheckout.multiply(new BigDecimal("0.03"));
                    row.commission = commCheckin.add(commCheckout);

                    row.totalSalary = row.baseSalary.add(row.commission);

                    list.add(row);
                }
            }
        }
        return list;
    }
}
