package com.example.backup.service;

public class ApiConfig {
    // 10.0.2.2 là localhost của máy tính khi chạy trên Android Emulator.
    // Nếu chạy trên điện thoại thật qua WiFi, hãy đổi thành IP máy tính, ví dụ: "http://192.168.1.15:8080/MovieBackend"
    public static final String BASE_URL = "http://10.0.2.2:8080/MovieBackend";

    public static final String URL_MOVIES = BASE_URL + "/movies";
    public static final String URL_GENRES = BASE_URL + "/genres";
    public static final String URL_AUTH = BASE_URL + "/auth";
    public static final String URL_FAVORITES = BASE_URL + "/favorites";
    public static final String URL_COMMENTS = BASE_URL + "/comments";
    public static final String URL_HISTORY = BASE_URL + "/history";
    public static final String URL_PROFILE = BASE_URL + "/profile";
}
