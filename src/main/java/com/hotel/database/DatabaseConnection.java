package com.hotel.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConnection {
    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            Properties props = new Properties();
            InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("database.properties");
            if (input != null) {
                props.load(input);
                url = props.getProperty("db.url");
                user = props.getProperty("db.user");
                password = props.getProperty("db.password");
                input.close();
            }
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
