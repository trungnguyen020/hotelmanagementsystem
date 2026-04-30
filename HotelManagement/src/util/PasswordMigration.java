package util;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PasswordMigration {
    public static void main(String[] args) {
        System.out.println("Bắt đầu tiến trình chuyển đổi mật khẩu sang chuẩn BCrypt...");
        int count = 0;
        int skipCount = 0;

        String selectSql = "SELECT id, username, password_hash FROM employees";
        String updateSql = "UPDATE employees SET password_hash = ? WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement psSelect = c.prepareStatement(selectSql);
             ResultSet rs = psSelect.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String pw = rs.getString("password_hash");

                // Nếu password đã là bcrypt (bắt đầu bằng $2a$) hoặc null thì bỏ qua
                if (pw == null || pw.startsWith("$2a$")) {
                    skipCount++;
                    System.out.println("Bỏ qua tài khoản '" + username + "' (đã được hash hoặc null).");
                    continue;
                }

                // Mã hóa password cũ
                String hashedPw = BCrypt.hashpw(pw, BCrypt.gensalt());

                // Cập nhật vào DB
                try (PreparedStatement psUpdate = c.prepareStatement(updateSql)) {
                    psUpdate.setString(1, hashedPw);
                    psUpdate.setInt(2, id);
                    psUpdate.executeUpdate();
                }

                System.out.println("Đã mã hóa thành công cho tài khoản '" + username + "'.");
                count++;
            }

            System.out.println("=========================================");
            System.out.println("Hoàn tất! Tổng số tài khoản đã chuyển đổi: " + count);
            System.out.println("Số tài khoản đã bỏ qua: " + skipCount);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Đã xảy ra lỗi trong quá trình chuyển đổi mật khẩu.");
        }
    }
}
