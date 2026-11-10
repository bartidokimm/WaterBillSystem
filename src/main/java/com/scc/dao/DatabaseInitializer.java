package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize()
            throws SQLException {

        String sql = "CREATE TABLE IF NOT EXISTS Users ("
                + "user_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password_hash TEXT NOT NULL, "
                + "role TEXT NOT NULL DEFAULT 'CUSTOMER', "
                + "status TEXT NOT NULL DEFAULT 'ACTIVE'"
                + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
        }
    }
}