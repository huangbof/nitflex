package com.example.backup.model;

import java.sql.Timestamp;

public class WatchHistory {
    private int id;
    private int userId;
    private int movieId;
    private Timestamp watchedAt;
    private int progress;

    public WatchHistory() {}

    public WatchHistory(int id, int userId, int movieId, Timestamp watchedAt, int progress) {
        this.id = id;
        this.userId = userId;
        this.movieId = movieId;
        this.watchedAt = watchedAt;
        this.progress = progress;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public Timestamp getWatchedAt() { return watchedAt; }
    public void setWatchedAt(Timestamp watchedAt) { this.watchedAt = watchedAt; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    @Override
    public String toString() {
        return "WatchHistory{id=" + id + ", userId=" + userId + ", movieId=" + movieId + ", progress=" + progress + "}";
    }
}
