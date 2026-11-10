package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    private static final String SELECT_COLUMNS =
            "SELECT id, user_id, action_type, description, timestamp "
                    + "FROM activity_logs ";

    // Never lets a logging failure crash the action being logged.
    public boolean logAction(int userId, String actionType,
                             String description) {

        String sql = "INSERT INTO activity_logs "
                + "(user_id, action_type, description) "
                + "VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, actionType);
            ps.setString(3, description);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.err.println(
                    "Warning: could not write audit log: "
                            + e.getMessage()
            );
            return false;
        }
    }

    public List<ActivityLog> getAllLogs() throws SQLException {
        String sql = SELECT_COLUMNS
                + "ORDER BY timestamp DESC, id DESC";

        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                logs.add(mapRow(rs));
            }
        }

        return logs;
    }

    public List<ActivityLog> getLogsByUserId(int userId)
            throws SQLException {

        String sql = SELECT_COLUMNS
                + "WHERE user_id = ? "
                + "ORDER BY timestamp DESC, id DESC";

        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        }

        return logs;
    }

    // Returns how many log rows were removed.
    public int deleteAllLogs() throws SQLException {
        String sql = "DELETE FROM activity_logs";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            return ps.executeUpdate();
        }
    }

    private ActivityLog mapRow(ResultSet rs) throws SQLException {
        return new ActivityLog(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("action_type"),
                rs.getString("description"),
                rs.getString("timestamp")
        );
    }
}