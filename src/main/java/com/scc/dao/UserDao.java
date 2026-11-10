package com.scc.dao;

import com.scc.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public boolean register(String username, String password, String role)
            throws SQLException {

        if (username == null || username.trim().isEmpty()
                || password == null || password.isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO Users "
                + "(username, password_hash, role, status) "
                + "VALUES (?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, password);
            ps.setString(3, role);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {

            if (e.getErrorCode() == 19
                    || (e.getMessage() != null
                    && e.getMessage().toLowerCase()
                    .contains("unique constraint"))) {
                return false;
            }

            throw e;
        }
    }

    public User login(String username, String password)
            throws SQLException {

        if (username == null || username.trim().isEmpty()
                || password == null || password.isEmpty()) {
            return null;
        }

        String sql = "SELECT user_id, username, password_hash, "
                + "role, status FROM Users "
                + "WHERE username = ? "
                + "AND password_hash = ? "
                + "AND status IN ('ACTIVE', 'APPROVED')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role"),
                            rs.getString("status")
                    );
                }
            }
        }

        return null;
    }

    public List<User> getAllUsers()
            throws SQLException {

        String sql = "SELECT user_id, username, password_hash, "
                + "role, status FROM Users ORDER BY user_id";

        return queryUsers(sql);
    }

    public List<User> getPendingUsers()
            throws SQLException {

        String sql = "SELECT user_id, username, password_hash, "
                + "role, status FROM Users "
                + "WHERE status = 'PENDING' ORDER BY user_id";

        return queryUsers(sql);
    }

    public boolean approveUser(int adminId, int userId)
            throws SQLException {

        String sql = "UPDATE Users SET status = 'APPROVED' "
                + "WHERE user_id = ? AND status = 'PENDING'";

        boolean updated;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            updated = ps.executeUpdate() == 1;
        }

        if (updated) {
            activityLogDao.logAction(adminId, "USER_APPROVED",
                    "Admin " + adminId
                            + " approved account for User ID " + userId);
        }

        return updated;
    }

    public boolean updateUserRoleAndStatus(int adminId, int userId,
                                           String newRole,
                                           String newStatus)
            throws SQLException {

        if (newRole == null || newStatus == null) {
            return false;
        }

        String role = newRole.trim().toUpperCase();
        String status = newStatus.trim().toUpperCase();

        String sql = "UPDATE Users SET role = ?, status = ? "
                + "WHERE user_id = ?";

        boolean updated;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role);
            ps.setString(2, status);
            ps.setInt(3, userId);

            updated = ps.executeUpdate() == 1;
        }

        if (updated) {
            activityLogDao.logAction(adminId, "USER_UPDATED",
                    "Admin " + adminId + " changed User " + userId
                            + " to role " + role
                            + ", status " + status);
        }

        return updated;
    }

    private List<User> queryUsers(String sql)
            throws SQLException {

        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }
        }

        return users;
    }
}