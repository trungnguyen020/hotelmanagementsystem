package dao;

import model.Service;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    public List<Service> findActive() throws SQLException {
        String sql = "SELECT id, name, unit_price, unit, active FROM services WHERE active=TRUE ORDER BY name";
        List<Service> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Service s = new Service();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setUnitPrice(rs.getBigDecimal("unit_price"));
                s.setUnit(rs.getString("unit"));
                s.setActive(rs.getBoolean("active"));
                list.add(s);
            }
        }
        return list;
    }
}