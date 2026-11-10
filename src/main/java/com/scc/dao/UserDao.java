package com.scc.dao;

import com.scc.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    public boolean register(String username, String password)
            throws SQLException {

        if (username == null || username.trim().isEmpty()
                || password == null || password.isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO Users "
                + "(username, password_hash, role, status) "
                + "VALUES (?, ?, 'CUSTOMER', 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, password);

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
                + "AND status = 'ACTIVE'";

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
}