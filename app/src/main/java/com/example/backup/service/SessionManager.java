package com.example.backup.service;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static final String PREF_NAME = "MovieAppSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_VIP = "is_vip";
    private static final String KEY_IS_ADMIN = "is_admin";
    private static final String KEY_WATCH_HISTORY = "local_watch_history";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn, int userId, String username) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }

    public void setVip(boolean isVip) {
        editor.putBoolean(KEY_IS_VIP, isVip);
        editor.apply();
    }

    public boolean isVip() {
        return pref.getBoolean(KEY_IS_VIP, false);
    }

    public void setAdmin(boolean isAdmin) {
        editor.putBoolean(KEY_IS_ADMIN, isAdmin);
        editor.apply();
    }

    public boolean isAdmin() {
        return pref.getBoolean(KEY_IS_ADMIN, false) || "admin".equalsIgnoreCase(getUsername());
    }

    // ==========================================
    // LƯU LỊCH SỬ XEM PHIM CỤC BỘ (Offline Cache)
    // ==========================================
    public void addWatchedMovie(int movieId) {
        if (movieId <= 0) return;
        String history = pref.getString(KEY_WATCH_HISTORY, "");
        List<String> list = new ArrayList<>();
        if (!history.isEmpty()) {
            String[] parts = history.split(",");
            for (String p : parts) {
                String clean = p.trim();
                if (!clean.isEmpty() && !clean.equals(String.valueOf(movieId))) {
                    list.add(clean);
                }
            }
        }
        // Luôn đưa phim vừa xem lên vị trí đầu tiên
        list.add(0, String.valueOf(movieId));

        // Giới hạn tối đa 30 phim gần nhất
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(list.size(), 30); i++) {
            if (i > 0) sb.append(",");
            sb.append(list.get(i));
        }
        editor.putString(KEY_WATCH_HISTORY, sb.toString()).apply();
    }

    public void removeWatchedMovie(int movieId) {
        if (movieId <= 0) return;
        String history = pref.getString(KEY_WATCH_HISTORY, "");
        if (history.isEmpty()) return;
        List<String> list = new ArrayList<>();
        for (String p : history.split(",")) {
            String clean = p.trim();
            if (!clean.isEmpty() && !clean.equals(String.valueOf(movieId))) {
                list.add(clean);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(list.get(i));
        }
        editor.putString(KEY_WATCH_HISTORY, sb.toString());
        editor.remove("watch_progress_" + movieId);
        editor.remove("watch_duration_" + movieId);
        editor.apply();
    }

    public void saveLocalWatchProgress(int movieId, int progressSec, int durationSec) {
        if (movieId <= 0) return;
        addWatchedMovie(movieId);
        editor.putInt("watch_progress_" + movieId, progressSec);
        if (durationSec > 0) {
            editor.putInt("watch_duration_" + movieId, durationSec);
        }
        editor.apply();
    }

    public int getLocalWatchProgress(int movieId) {
        return pref.getInt("watch_progress_" + movieId, 0);
    }

    public int getLocalWatchDuration(int movieId) {
        return pref.getInt("watch_duration_" + movieId, 0);
    }

    public List<Integer> getWatchedMovieIds() {
        String history = pref.getString(KEY_WATCH_HISTORY, "");
        List<Integer> list = new ArrayList<>();
        if (!history.isEmpty()) {
            for (String p : history.split(",")) {
                try {
                    list.add(Integer.parseInt(p.trim()));
                } catch (Exception ignored) {}
            }
        }
        return list;
    }

    public void clearLocalWatchHistory() {
        editor.remove(KEY_WATCH_HISTORY).apply();
    }

    // ==========================================
    // LƯU PHIM YÊU THÍCH CỤC BỘ (Offline Cache)
    // ==========================================
    private static final String KEY_FAVORITES = "local_favorites";

    public void addLocalFavorite(int movieId) {
        if (movieId <= 0) return;
        List<Integer> list = getLocalFavoriteIds();
        if (!list.contains(movieId)) {
            list.add(movieId);
            saveLocalFavoriteIds(list);
        }
    }

    public void removeLocalFavorite(int movieId) {
        List<Integer> list = getLocalFavoriteIds();
        if (list.remove(Integer.valueOf(movieId))) {
            saveLocalFavoriteIds(list);
        }
    }

    public boolean isLocalFavorite(int movieId) {
        return getLocalFavoriteIds().contains(movieId);
    }

    public List<Integer> getLocalFavoriteIds() {
        String raw = pref.getString(KEY_FAVORITES, "");
        List<Integer> list = new ArrayList<>();
        if (!raw.isEmpty()) {
            for (String p : raw.split(",")) {
                try {
                    list.add(Integer.parseInt(p.trim()));
                } catch (Exception ignored) {}
            }
        }
        return list;
    }

    private void saveLocalFavoriteIds(List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(list.get(i));
        }
        editor.putString(KEY_FAVORITES, sb.toString()).apply();
    }

    // ==========================================
    // LƯU ĐÁNH GIÁ PHIM CỤC BỘ (Offline Cache)
    // ==========================================
    public void saveLocalRating(int movieId, float rating) {
        editor.putFloat("rating_movie_" + movieId, rating).apply();
    }

    public float getLocalRating(int movieId) {
        return pref.getFloat("rating_movie_" + movieId, 0f);
    }

    // ==========================================
    // LƯU BÌNH LUẬN PHIM CỤC BỘ (Offline Cache)
    // ==========================================
    public void addLocalComment(int movieId, String username, String content) {
        String key = "local_comments_" + movieId;
        String existing = pref.getString(key, "");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        String timeStr = sdf.format(new java.util.Date());

        String newEntry = username + ":::" + content + ":::" + timeStr;
        if (!existing.isEmpty()) {
            existing = newEntry + ";;;" + existing;
        } else {
            existing = newEntry;
        }
        editor.putString(key, existing).apply();
    }

    public List<com.example.backup.model.Comment> getLocalComments(int movieId) {
        List<com.example.backup.model.Comment> list = new ArrayList<>();
        String key = "local_comments_" + movieId;
        String raw = pref.getString(key, "");
        if (!raw.isEmpty()) {
            String[] entries = raw.split(";;;");
            int idCounter = 1000;
            for (String entry : entries) {
                String[] parts = entry.split(":::");
                if (parts.length >= 3) {
                    com.example.backup.model.Comment c = new com.example.backup.model.Comment();
                    c.setId(idCounter++);
                    c.setMovieId(movieId);
                    c.setUsername(parts[0]);
                    c.setContent(parts[1]);
                    c.setCreatedAt(parts[2]);
                    list.add(c);
                }
            }
        }
        return list;
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
