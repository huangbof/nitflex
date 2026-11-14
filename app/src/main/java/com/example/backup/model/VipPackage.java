package com.example.backup.model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Locale;

public class VipPackage implements Serializable {
    private int id;
    private String name;
    private String code;
    private double price;
    private int durationDays;
    private String description;
    private boolean isActive = true;
    private String createdAt;

    public VipPackage() {}

    public VipPackage(int id, String name, String code, double price, int durationDays, String description, boolean isActive) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.price = price;
        this.durationDays = durationDays;
        this.description = description;
        this.isActive = isActive;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getFormattedPrice() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return nf.format(price);
    }
}
