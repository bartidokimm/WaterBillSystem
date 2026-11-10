package com.scc.model;

public class User {

    private int userId;
    private String username;
    private String passwordHash;
    private String role;
    private String status;

    public User(int userId, String username,
                String passwordHash, String role,
                String status) {

        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }
}