package com.example.backup.model;

import java.sql.Timestamp;

public class Favorite {
    private int id;
    private int userId;
    private int movieId;
    private Timestamp addedAt;

    public Favorite() {}

    public Favorite(int id, int userId, int movieId, Timestamp addedAt) {
        this.id = id;
        this.userId = userId;
        this.movieId = movieId;
        this.addedAt = addedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public Timestamp getAddedAt() { return addedAt; }
    public void setAddedAt(Timestamp addedAt) { this.addedAt = addedAt; }

    @Override
    public String toString() {
        return "Favorite{id=" + id + ", userId=" + userId + ", movieId=" + movieId + "}";
    }
}
