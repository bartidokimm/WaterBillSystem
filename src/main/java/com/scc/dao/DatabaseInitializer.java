package com.scc.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize()
            throws SQLException {

        // Parent tables first, so foreign keys always have a table to point to
        String users = "CREATE TABLE IF NOT EXISTS Users ("
                + "user_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password_hash TEXT NOT NULL, "
                + "role TEXT NOT NULL DEFAULT 'CUSTOMER', "
                + "status TEXT NOT NULL DEFAULT 'ACTIVE'"
                + ")";

        String customers = "CREATE TABLE IF NOT EXISTS Customers ("
                + "customer_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER, "
                + "customer_name TEXT NOT NULL, "
                + "address TEXT, "
                + "contact_number TEXT, "
                + "status TEXT NOT NULL DEFAULT 'ACTIVE', "
                + "FOREIGN KEY (user_id) REFERENCES Users(user_id)"
                + ")";

        String bills = "CREATE TABLE IF NOT EXISTS Bills ("
                + "bill_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "customer_id INTEGER NOT NULL, "
                + "previous_reading REAL NOT NULL DEFAULT 0, "
                + "current_reading REAL NOT NULL DEFAULT 0, "
                + "consumption REAL NOT NULL DEFAULT 0, "
                + "bill_amount REAL NOT NULL DEFAULT 0, "
                + "bill_date TEXT NOT NULL, "
                + "payment_status TEXT NOT NULL DEFAULT 'UNPAID', "
                + "FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)"
                + ")";

        String payments = "CREATE TABLE IF NOT EXISTS Payments ("
                + "payment_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "bill_id INTEGER NOT NULL, "
                + "payment_date TEXT NOT NULL, "
                + "amount_paid REAL NOT NULL, "
                + "payment_status TEXT NOT NULL DEFAULT 'PAID', "
                + "FOREIGN KEY (bill_id) REFERENCES Bills(bill_id)"
                + ")";

        String activityLogs = "CREATE TABLE IF NOT EXISTS activity_logs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "action_type TEXT NOT NULL, "
                + "description TEXT NOT NULL, "
                + "timestamp TEXT NOT NULL "
                + "DEFAULT (datetime('now', 'localtime')), "
                + "FOREIGN KEY (user_id) REFERENCES Users(user_id) "
                + "ON DELETE CASCADE"
                + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(users);
            stmt.execute(customers);
            stmt.execute(bills);
            stmt.execute(payments);

            dropOldActivityLogsTable(conn);
            stmt.execute(activityLogs);
        }
    }

    // Earlier deliverables created Activity_Logs with different columns.
    // SQLite table names are case-insensitive, so CREATE IF NOT EXISTS would
    // silently keep that old table. It was never written to, so replace it.
    private static void dropOldActivityLogsTable(Connection conn)
            throws SQLException {

        boolean tableExists = false;
        boolean hasActionType = false;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "PRAGMA table_info(activity_logs)")) {

            while (rs.next()) {
                tableExists = true;
                if ("action_type".equalsIgnoreCase(rs.getString("name"))) {
                    hasActionType = true;
                }
            }
        }

        if (tableExists && !hasActionType) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE activity_logs");
            }
        }
    }
}