package com.example.backup.repository;

import com.example.backup.model.Genre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GenreRepository {

    private Connection getConn() {
        return DBConnection.getInstance().getConnection();
    }

    private Genre mapRow(ResultSet rs) throws SQLException {
        return new Genre(rs.getInt("id"), rs.getString("name"));
    }

    public List<Genre> findAll() {
        List<Genre> genres = new ArrayList<>();
        String sql = "SELECT * FROM genres ORDER BY name ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) genres.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genres;
    }

    public Genre findById(int id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
