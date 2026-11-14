package com.example.backup.model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardStats implements Serializable {
    private int totalUsers;
    private int vipUsers;
    private int totalMovies;
    private int vipMovies;
    private int totalViews;
    private int totalComments;
    private int totalRatings;
    private double estimatedRevenue;
    private List<Movie> topMovies = new ArrayList<>();

    public DashboardStats() {}

    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }

    public int getVipUsers() { return vipUsers; }
    public void setVipUsers(int vipUsers) { this.vipUsers = vipUsers; }

    public int getRegularUsers() { return Math.max(0, totalUsers - vipUsers); }

    public int getTotalMovies() { return totalMovies; }
    public void setTotalMovies(int totalMovies) { this.totalMovies = totalMovies; }

    public int getVipMovies() { return vipMovies; }
    public void setVipMovies(int vipMovies) { this.vipMovies = vipMovies; }

    public int getRegularMovies() { return Math.max(0, totalMovies - vipMovies); }

    public int getTotalViews() { return totalViews; }
    public void setTotalViews(int totalViews) { this.totalViews = totalViews; }

    public int getTotalComments() { return totalComments; }
    public void setTotalComments(int totalComments) { this.totalComments = totalComments; }

    public int getTotalRatings() { return totalRatings; }
    public void setTotalRatings(int totalRatings) { this.totalRatings = totalRatings; }

    public double getEstimatedRevenue() { return estimatedRevenue; }
    public void setEstimatedRevenue(double estimatedRevenue) { this.estimatedRevenue = estimatedRevenue; }

    public List<Movie> getTopMovies() { return topMovies; }
    public void setTopMovies(List<Movie> topMovies) { this.topMovies = topMovies; }

    public String getFormattedRevenue() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return nf.format(estimatedRevenue);
    }

    public String getVipConversionRate() {
        if (totalUsers <= 0) return "0.0%";
        double rate = ((double) vipUsers / totalUsers) * 100.0;
        return String.format(Locale.US, "%.1f%%", rate);
    }
}
