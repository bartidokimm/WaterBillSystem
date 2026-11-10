package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:sqlite:water_billing.db";

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase()
            throws SQLException {

        String sql = "CREATE TABLE IF NOT EXISTS Users ("
                + "user_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password_hash TEXT NOT NULL, "
                + "role TEXT NOT NULL DEFAULT 'CUSTOMER', "
                + "status TEXT NOT NULL DEFAULT 'ACTIVE'"
                + ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
        }
    }
}