package com.example.backup.model;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String username;
    private String email;
    private String avatarUrl;
    private boolean isVip;
    private String role; // "admin" | "user"
    private String createdAt;

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public boolean isVip() { return isVip; }
    public void setVip(boolean vip) { isVip = vip; }

    public String getRole() { return role != null ? role : "user"; }
    public void setRole(String role) { this.role = role; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(username);
    }
}
