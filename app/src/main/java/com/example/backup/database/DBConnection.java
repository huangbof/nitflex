package com.example.backup.database;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String TAG = "DBConnection";

    // 10.0.2.2 là địa chỉ IP của máy tính khi chạy trên máy ảo Android Emulator.
    // Nếu bạn test trên điện thoại thật cắm dây/qua WiFi, hãy đổi thành IP máy tính (ví dụ: "192.168.1.15").
    public static String DB_HOST = "10.0.2.2";
    public static String DB_PORT = "3306";
    public static String DB_NAME = "movie_db";
    public static String DB_USER = "root";
    public static String DB_PASS = "";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Load driver MySQL
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME 
                    + "?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true"
                    + "&connectTimeout=5000&socketTimeout=8000&useSSL=false";
            
            conn = DriverManager.getConnection(url, DB_USER, DB_PASS);
            Log.d(TAG, "Kết nối MySQL thành công!");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Không tìm thấy MySQL Driver: " + e.getMessage());
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi kết nối MySQL: " + e.getMessage());
        }
        return conn;
    }
}
