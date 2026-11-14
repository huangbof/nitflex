package com.example.backup.service;

import com.example.backup.model.User;
import com.example.backup.repository.UserRepository;

import java.security.MessageDigest;

public class UserService {

    private final UserRepository userRepo = new UserRepository();

    // -----------------------------------------------------------------------
    // login — hash the supplied password with MD5, then compare
    // -----------------------------------------------------------------------
    public User login(String username, String password) {
        if (username == null || password == null) return null;
        User user = userRepo.findByUsername(username);
        if (user == null) return null;
        String hashed = md5(password);
        if (hashed != null && hashed.equals(user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // register — check duplicates, hash password, persist
    // -----------------------------------------------------------------------
    public boolean register(String username, String password, String email) {
        if (username == null || password == null || email == null) return false;
        if (userRepo.existsByUsername(username)) return false;
        if (userRepo.existsByEmail(email)) return false;

        String hashed = md5(password);
        if (hashed == null) return false;

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(hashed);
        user.setEmail(email);
        user.setAvatarUrl(null);

        int newId = userRepo.save(user);
        return newId > 0;
    }

    // -----------------------------------------------------------------------
    // getProfile
    // -----------------------------------------------------------------------
    public User getProfile(int userId) {
        return userRepo.findById(userId);
    }

    // -----------------------------------------------------------------------
    // updateAvatar
    // -----------------------------------------------------------------------
    public boolean updateAvatar(int userId, String avatarUrl) {
        return userRepo.updateAvatar(userId, avatarUrl);
    }

    // -----------------------------------------------------------------------
    // MD5 helper
    // -----------------------------------------------------------------------
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
