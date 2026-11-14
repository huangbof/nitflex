package com.example.backup.model;

import java.io.Serializable;

public class Movie implements Serializable {
    private int id;
    private int tmdbId;
    private String title;
    private String originalTitle;
    private String overview;
    private String posterPath;
    private String backdropPath;
    private String releaseDate;
    private double voteAverage;
    private int voteCount;
    private double popularity;
    private String originalLanguage;
    private boolean adult;
    private String videoUrl;
    private int genreId;
    private int viewCount;

    // Tiến độ xem phim (phục vụ tính năng Tiếp Tục Xem & Lịch Sử Xem)
    private int watchProgress; // Số giây đã xem
    private int watchDuration; // Tổng thời lượng phim (giây)

    public Movie() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTmdbId() { return tmdbId; }
    public void setTmdbId(int tmdbId) { this.tmdbId = tmdbId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOriginalTitle() { return originalTitle; }
    public void setOriginalTitle(String originalTitle) { this.originalTitle = originalTitle; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getBackdropPath() { return backdropPath; }
    public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(double voteAverage) { this.voteAverage = voteAverage; }

    public int getVoteCount() { return voteCount; }
    public void setVoteCount(int voteCount) { this.voteCount = voteCount; }

    public double getPopularity() { return popularity; }
    public void setPopularity(double popularity) { this.popularity = popularity; }

    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }

    public boolean isAdult() { return adult; }
    public void setAdult(boolean adult) { this.adult = adult; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public int getGenreId() { return genreId; }
    public void setGenreId(int genreId) { this.genreId = genreId; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public int getWatchProgress() { return watchProgress; }
    public void setWatchProgress(int watchProgress) { this.watchProgress = watchProgress; }

    public int getWatchDuration() { return watchDuration; }
    public void setWatchDuration(int watchDuration) { this.watchDuration = watchDuration; }

    /**
     * Tính toán phần trăm đã xem (1% - 100%)
     */
    public int getWatchProgressPercent() {
        if (watchProgress <= 0) return 0;
        int total = watchDuration > 0 ? watchDuration : 7200; // Mặc định 120 phút nếu chưa có duration
        int percent = (int) Math.round(((double) watchProgress / total) * 100);
        return Math.max(1, Math.min(100, percent));
    }

    /**
     * Định dạng thời gian đã xem, ví dụ "14:20" hoặc "1h15p"
     */
    public String getFormattedWatchProgress() {
        if (watchProgress <= 0) return "00:00";
        int hours = watchProgress / 3600;
        int minutes = (watchProgress % 3600) / 60;
        int seconds = watchProgress % 60;
        if (hours > 0) {
            return String.format(java.util.Locale.getDefault(), "%dh%02dp", hours, minutes);
        } else {
            return String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Số phút còn lại của phim
     */
    public String getRemainingTimeText() {
        int total = watchDuration > 0 ? watchDuration : 7200;
        int remainingSec = Math.max(0, total - watchProgress);
        int remMin = remainingSec / 60;
        if (remMin >= 60) {
            return "Còn " + (remMin / 60) + "h" + (remMin % 60) + "p";
        }
        return "Còn " + remMin + " phút";
    }

    /**
     * Phân loại phim VIP: Phim đạt điểm số đánh giá TMDB > 8.0
     */
    public boolean isVip() {
        return voteAverage > 8.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return id == movie.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
