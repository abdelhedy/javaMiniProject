package org.projectmanagement.util;

import java.sql.Connection;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseUtil {
    private static Connection connection;

    // Static block - executed once when class is loaded into memory
    static {
        try {
            Properties props = new Properties();
            InputStream is = null;

            // Try to load db.properties
            is = DatabaseUtil.class.getClassLoader().getResourceAsStream("db.properties");

            if (is == null) {
                is = new java.io.FileInputStream("src/main/resources/db.properties");
            }

            props.load(is);
            is.close();

            String driver = props.getProperty("db.driver");
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            // Load driver
            Class.forName(driver);
            System.out.println("Driver OK");

            // Create single connection
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connection OK");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Returns the single shared connection.
     * IMPORTANT: Do NOT close this connection in your DAOs!
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * Closes the connection (only call when application shuts down)
     */
    public static void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connection closed");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tests if connection is still valid
     */
    public static boolean testConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
