package util;

import java.sql.Connection;
import java.sql.Statement;

public class CreateDiscountRulesTable {
    public static void main(String[] args) {
        String sql = "CREATE TABLE IF NOT EXISTS discount_rules (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "min_days INT NOT NULL UNIQUE, " +
                     "discount_percent DECIMAL(5,2) NOT NULL" +
                     ");";
        
        String insertSql = "INSERT IGNORE INTO discount_rules (min_days, discount_percent) VALUES (10, 10.00), (20, 20.00);";

        try (Connection c = DBConnection.getConnection();
             Statement stmt = c.createStatement()) {
            
            stmt.executeUpdate(sql);
            System.out.println("Tạo bảng discount_rules thành công.");
            
            stmt.executeUpdate(insertSql);
            System.out.println("Thêm dữ liệu mẫu vào bảng discount_rules thành công.");
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
