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

            // --- Pricing redesign migrations ---
            System.out.println("Adding price_per_hour to room_types...");
            try {
                stmt.executeUpdate("ALTER TABLE room_types ADD COLUMN price_per_hour DECIMAL(12,2) DEFAULT 0;");
                System.out.println("Success.");
            } catch (Exception e) {
                System.out.println("Column might already exist: " + e.getMessage());
            }

            System.out.println("Adding price_overnight to room_types...");
            try {
                stmt.executeUpdate("ALTER TABLE room_types ADD COLUMN price_overnight DECIMAL(12,2) DEFAULT 0;");
                System.out.println("Success.");
            } catch (Exception e) {
                System.out.println("Column might already exist: " + e.getMessage());
            }

            System.out.println("Setting default hourly/overnight prices based on daily price...");
            try {
                // Hourly = daily / 12 (so 12 hours = 1 day price, making hourly more expensive per unit)
                stmt.executeUpdate("UPDATE room_types SET price_per_hour = ROUND(price_per_night / 12, 0) WHERE price_per_hour = 0 OR price_per_hour IS NULL;");
                // Overnight = daily * 0.5 (overnight is cheaper than full day)
                stmt.executeUpdate("UPDATE room_types SET price_overnight = ROUND(price_per_night * 0.5, 0) WHERE price_overnight = 0 OR price_overnight IS NULL;");
                System.out.println("Success.");
            } catch (Exception e) {
                System.out.println("Error setting defaults: " + e.getMessage());
            }

            System.out.println("Adding pricing_type to stays...");
            try {
                stmt.executeUpdate("ALTER TABLE stays ADD COLUMN pricing_type VARCHAR(20) DEFAULT 'DAILY';");
                System.out.println("Success.");
            } catch (Exception e) {
                System.out.println("Column might already exist: " + e.getMessage());
            }

            System.out.println("Migration completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
