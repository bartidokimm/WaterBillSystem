package com.scc.dao;

import com.scc.model.Bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BillDao {

    // Placeholder rate: change it to match your approved proposal.
    public static final double RATE_PER_CUBIC_METER = 25.00;

    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    // Core business process. Returns the new bill ID, or -1 on invalid input.
    public int processBill(int staffUserId, int customerId,
                           double previousReading, double currentReading)
            throws SQLException {

        if (previousReading < 0 || currentReading < previousReading) {
            return -1;
        }

        double consumption = currentReading - previousReading;
        double amount = consumption * RATE_PER_CUBIC_METER;

        String customerSql = "SELECT customer_name FROM Customers "
                + "WHERE customer_id = ?";

        String insertSql = "INSERT INTO Bills "
                + "(customer_id, previous_reading, current_reading, "
                + "consumption, bill_amount, bill_date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        String customerName;
        int billId = -1;

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(customerSql)) {
                ps.setInt(1, customerId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return -1;
                    }
                    customerName = rs.getString("customer_name");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    insertSql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, customerId);
                ps.setDouble(2, previousReading);
                ps.setDouble(3, currentReading);
                ps.setDouble(4, consumption);
                ps.setDouble(5, amount);
                ps.setString(6, LocalDate.now().toString());

                if (ps.executeUpdate() == 1) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            billId = keys.getInt(1);
                        }
                    }
                }
            }

            if (billId > 0) {
                activityLogDao.logAction(staffUserId, "BILL_PROCESSED",
                        String.format(
                                "Processed bill #%d for Customer %s "
                                        + "- Total: P%.2f",
                                billId, customerName, amount));
            }
        }

        return billId;
    }

    // Lifecycle / state change: UNPAID <-> PAID.
    public boolean updatePaymentStatus(int staffUserId, int billId,
                                       String newStatus)
            throws SQLException {

        if (newStatus == null) {
            return false;
        }

        String status = newStatus.trim().toUpperCase();

        String selectSql = "SELECT payment_status FROM Bills "
                + "WHERE bill_id = ?";
        String updateSql = "UPDATE Bills SET payment_status = ? "
                + "WHERE bill_id = ?";

        String oldStatus;
        boolean updated;

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, billId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    oldStatus = rs.getString("payment_status");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, status);
                ps.setInt(2, billId);
                updated = ps.executeUpdate() == 1;
            }

            if (updated) {
                activityLogDao.logAction(staffUserId,
                        "BILL_UPDATED",
                        "Updated Bill #" + billId + " status from "
                                + oldStatus + " to " + status);
            }
        }

        return updated;
    }

    public List<Bill> getAllBills() throws SQLException {
        String sql = "SELECT bill_id, customer_id, consumption, "
                + "bill_amount, bill_date, payment_status FROM Bills "
                + "ORDER BY bill_id DESC";

        List<Bill> bills = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                bills.add(new Bill(
                        rs.getInt("bill_id"),
                        rs.getInt("customer_id"),
                        rs.getDouble("consumption"),
                        rs.getDouble("bill_amount"),
                        rs.getString("bill_date"),
                        rs.getString("payment_status")
                ));
            }
        }

        return bills;
    }
}