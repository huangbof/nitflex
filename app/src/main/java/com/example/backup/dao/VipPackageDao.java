package com.example.backup.dao;

import com.example.backup.database.DBConnection;
import com.example.backup.model.VipPackage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object chuyên xử lý truy vấn bảng vip_packages trong CSDL MySQL
 */
public class VipPackageDao {

    public static List<VipPackage> getAllVipPackages() throws SQLException {
        String sql = "SELECT * FROM vip_packages WHERE is_active = 1 ORDER BY price ASC";
        List<VipPackage> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VipPackage p = new VipPackage();
                    p.setId(rs.getInt("id"));
                    p.setName(rs.getString("name"));
                    try { p.setCode(rs.getString("code")); } catch (Exception ignored) {}
                    p.setPrice(rs.getDouble("price"));
                    p.setDurationDays(rs.getInt("duration_days"));
                    p.setDescription(rs.getString("description"));
                    try { p.setActive(rs.getBoolean("is_active")); } catch (Exception ignored) {}
                    list.add(p);
                }
            }
        }
        return list;
    }
}
