package com.scc.dao;

import com.scc.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    // Returns the new customer ID, or -1 if nothing was inserted.
    public int addCustomer(int staffUserId, String name,
                           String address, String contactNumber)
            throws SQLException {

        if (name == null || name.trim().isEmpty()) {
            return -1;
        }

        String sql = "INSERT INTO Customers "
                + "(customer_name, address, contact_number) "
                + "VALUES (?, ?, ?)";

        int customerId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name.trim());
            ps.setString(2, address);
            ps.setString(3, contactNumber);

            if (ps.executeUpdate() == 1) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        customerId = keys.getInt(1);
                    }
                }
            }
        }

        if (customerId > 0) {
            activityLogDao.logAction(staffUserId, "CUSTOMER_ADDED",
                    "Staff " + staffUserId + " added Customer "
                            + name.trim() + " (ID " + customerId + ")");
        }

        return customerId;
    }

    public List<Customer> getAllCustomers() throws SQLException {
        String sql = "SELECT customer_id, customer_name, address, "
                + "contact_number, status FROM Customers "
                + "ORDER BY customer_id";

        List<Customer> customers = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("address"),
                        rs.getString("contact_number"),
                        rs.getString("status")
                ));
            }
        }

        return customers;
    }
}