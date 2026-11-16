package com.example.backup.dao;

import com.example.backup.database.DBConnection;
import com.example.backup.model.Cast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CastDao {

    public static List<Cast> getMovieCasts(int movieId) throws SQLException {
        List<Cast> list = new ArrayList<>();
        String sql = "SELECT * FROM movie_casts "
                   + "WHERE movie_id = ? "
                   + "   OR movie_id IN (SELECT id FROM movies WHERE tmdb_id = ?) "
                   + "ORDER BY id ASC";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối MySQL!");
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, movieId);
                stmt.setInt(2, movieId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Cast c = new Cast();
                        c.setId(rs.getInt("id"));
                        c.setMovieId(rs.getInt("movie_id"));
                        c.setActorName(rs.getString("actor_name"));
                        c.setCharacterName(rs.getString("character_name"));
                        c.setProfilePath(rs.getString("profile_path"));
                        list.add(c);
                    }
                }
            }
        }
        return list;
    }
}
