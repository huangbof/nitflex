package com.example.backup.repository;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnection {

    private static DBConnection instance;
    private Connection connection;

    private static final String DEFAULT_URL      = "jdbc:mysql://localhost:3306/movie_db?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER     = "root";
    private static final String DEFAULT_PASSWORD = "";

    private DBConnection() {
        try {
            Properties props = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties");
            String url      = DEFAULT_URL;
            String user     = DEFAULT_USER;
            String password = DEFAULT_PASSWORD;

            if (is != null) {
                props.load(is);
                url      = props.getProperty("db.url",      DEFAULT_URL);
                user     = props.getProperty("db.user",     DEFAULT_USER);
                password = props.getProperty("db.password", DEFAULT_PASSWORD);
                is.close();
            }

            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    public static synchronized DBConnection getInstance() {
        try {
            if (instance == null || instance.connection == null || instance.connection.isClosed()) {
                instance = new DBConnection();
            }
        } catch (Exception e) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
