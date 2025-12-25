package org.projectmanagement.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseUtil {
    private static HikariDataSource dataSource;

    static {
        try {
            Properties props = new Properties();
            InputStream is = DatabaseUtil.class.getClassLoader().getResourceAsStream("db.properties");

            if (is == null) {
                System.err.println("ERROR: db.properties not found in classpath!");
                throw new RuntimeException("db.properties not found");
            }

            props.load(is);
            is.close();

            String driver = props.getProperty("db.driver");
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            // Load driver
            Class.forName(driver);
            System.out.println("✓ MySQL Driver loaded successfully");

            // Configure HikariCP connection pool
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);

            // Pool configuration
            config.setMaximumPoolSize(10);           // Max 10 connections
            config.setMinimumIdle(2);                // Keep 2 idle connections
            config.setConnectionTimeout(30000);       // 30 seconds to get connection
            config.setIdleTimeout(600000);           // 10 minutes idle timeout
            config.setMaxLifetime(1800000);          // 30 minutes max lifetime
            config.setAutoCommit(true);              // Auto-commit by default

            // MySQL specific optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

            System.out.println("✓ Connection pool initialized successfully");
            System.out.println("  Database: " + url);
            System.out.println("  Pool size: 2-10 connections");

            // Test connection
            try (Connection testConn = dataSource.getConnection()) {
                if (testConn.isValid(2)) {
                    System.out.println("✓ Database connection test successful");
                }
            }

        } catch (Exception e) {
            System.err.println("✗ Database initialization FAILED:");
            e.printStackTrace();
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Gets a connection from the pool.
     * IMPORTANT: This connection MUST be closed in a try-with-resources or finally block!
     * When closed, it returns to the pool (it's not actually closed).
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Connection pool is not available");
        }
        return dataSource.getConnection();
    }

    /**
     * Closes the connection pool completely (call on application shutdown only)
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("✓ Connection pool closed");
        }
    }

    /**
     * Tests if the pool is operational
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && conn.isValid(2);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets pool statistics for monitoring
     */
    public static void printPoolStats() {
        if (dataSource != null) {
            System.out.println("=== Connection Pool Stats ===");
            System.out.println("Active connections: " + (dataSource.getHikariPoolMXBean().getActiveConnections()));
            System.out.println("Idle connections: " + (dataSource.getHikariPoolMXBean().getIdleConnections()));
            System.out.println("Total connections: " + (dataSource.getHikariPoolMXBean().getTotalConnections()));
            System.out.println("Threads waiting: " + (dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()));
            System.out.println("=============================");
        }
    }
}