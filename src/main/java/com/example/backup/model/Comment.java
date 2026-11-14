package com.example.backup.model;

import java.sql.Timestamp;

public class Comment {
    private int id;
    private int userId;
    private int movieId;
    private String content;
    private Timestamp createdAt;
    // Populated via JOIN with users table
    private String username;

    public Comment() {}

    public Comment(int id, int userId, int movieId, String content,
                   Timestamp createdAt, String username) {
        this.id = id;
        this.userId = userId;
        this.movieId = movieId;
        this.content = content;
        this.createdAt = createdAt;
        this.username = username;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @Override
    public String toString() {
        return "Comment{id=" + id + ", userId=" + userId + ", movieId=" + movieId + "}";
    }
}
