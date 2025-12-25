package org.projectmanagement;

import org.projectmanagement.util.DatabaseUtil;

import java.sql.Connection;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("  Testing Database Connection");
        System.out.println("=================================\n");

        try {
            // Test 1: Get the connection
            Connection conn = DatabaseUtil.getConnection();

            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Connection successful!");
                System.out.println("✓ Connection is open and ready");

                // Test 2: Check if connection is valid
                if (DatabaseUtil.testConnection()) {
                    System.out.println("✓ Connection validation passed");
                } else {
                    System.out.println("✗ Connection validation failed");
                }

                System.out.println("\n=================================");
                System.out.println("  Database is ready to use!");
                System.out.println("=================================");

            } else {
                System.out.println("✗ Connection failed or is closed");
            }

        } catch (Exception e) {
            System.out.println("✗ Connection test failed!");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

        // Optional: Close connection when application ends
         DatabaseUtil.close();
    }
}