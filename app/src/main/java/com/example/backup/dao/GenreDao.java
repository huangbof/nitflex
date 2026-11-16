package com.example.backup.dao;

import com.example.backup.database.DBConnection;
import com.example.backup.model.Genre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenreDao {

    public static List<Genre> getAllGenres() throws SQLException {
        String sql = "SELECT * FROM genres ORDER BY name ASC";
        List<Genre> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối đến MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Genre(rs.getInt("id"), rs.getString("name")));
                }
            }
        }
        return list;
    }
}
