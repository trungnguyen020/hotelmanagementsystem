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
        public BigDecimal basicSalary;
        public int workedDays;
        public BigDecimal baseSalary;
        public BigDecimal revenueCheckin;
        public BigDecimal revenueCheckout;
        public BigDecimal commission;
        public BigDecimal totalSalary;
    }

    public List<SalaryReportRow> getSalaryReport(Integer month, Integer year) throws Exception {
        String cinSub = "SELECT s.created_by AS emp_id, SUM(i.total) AS total_checkin FROM invoices i JOIN stays s ON i.stay_id = s.id";
        String coutSub = "SELECT i.paid_by AS emp_id, SUM(i.total) AS total_checkout FROM invoices i";

        String attSub = "SELECT employee_id, COUNT(DISTINCT date) AS worked_days FROM attendance";

        if (month != null && year != null) {
            cinSub += " WHERE MONTH(i.paid_at) = ? AND YEAR(i.paid_at) = ?";
            coutSub += " WHERE MONTH(i.paid_at) = ? AND YEAR(i.paid_at) = ?";
            attSub += " WHERE MONTH(date) = ? AND YEAR(date) = ?";
        }
        cinSub += " GROUP BY s.created_by";
        coutSub += " GROUP BY i.paid_by";
        attSub += " GROUP BY employee_id";

        String sql = "SELECT e.id, e.username, e.full_name, e.basic_salary, r.code AS role_code, " +
                "  COALESCE(att.worked_days, 0) AS worked_days, " +
                "  COALESCE(cin.total_checkin, 0) AS revenue_checkin, " +
                "  COALESCE(cout.total_checkout, 0) AS revenue_checkout " +
                "FROM employees e " +
                "JOIN roles r ON e.role_id = r.id " +
                "LEFT JOIN (" + attSub + ") att ON att.employee_id = e.id " +
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
                ps.setInt(5, month);
                ps.setInt(6, year);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SalaryReportRow row = new SalaryReportRow();
                    row.empId = rs.getInt("id");
                    row.username = rs.getString("username");
                    row.fullName = rs.getString("full_name");
                    row.role = rs.getString("role_code");
                    row.basicSalary = rs.getBigDecimal("basic_salary");
                    if (row.basicSalary == null) row.basicSalary = new BigDecimal("7000000");
                    row.workedDays = rs.getInt("worked_days");
                    row.revenueCheckin = rs.getBigDecimal("revenue_checkin");
                    row.revenueCheckout = rs.getBigDecimal("revenue_checkout");

                    // Lương cơ bản thực lãnh = (Lương CB / 26) * số ngày làm (làm tròn)
                    BigDecimal days = new BigDecimal(row.workedDays);
                    row.baseSalary = row.basicSalary.divide(new BigDecimal("26"), 2, java.math.RoundingMode.HALF_UP).multiply(days);

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
