package util;

import java.sql.Connection;
import java.sql.Statement;

public class DBMigration {
    public static void main(String[] args) {
        try (Connection c = DBConnection.getConnection();
             Statement stmt = c.createStatement()) {
            
            System.out.println("Adding basic_salary to employees...");
            try {
                stmt.executeUpdate("ALTER TABLE employees ADD COLUMN basic_salary DECIMAL(15,2) DEFAULT 7000000.00;");
                System.out.println("Success.");
            } catch (Exception e) {
                System.out.println("Column might already exist: " + e.getMessage());
            }

            System.out.println("Creating attendance table...");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS attendance (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "employee_id INT, " +
                    "check_in_time DATETIME, " +
                    "date DATE, " +
                    "FOREIGN KEY (employee_id) REFERENCES employees(id)" +
                    ");");
            System.out.println("Success.");

            System.out.println("Migration completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
