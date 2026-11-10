package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private static final String[] VALID_ROLES = {
            "ADMIN", "CUSTOMER", "BILLING_STAFF", "CASHIER"
    };

    private static final String[] VALID_STATUSES = {
            "APPROVED", "PENDING"
    };

    private static final int CANCELLED = 0;

    private final Scanner scanner;
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;

    public AdminMenu(Scanner scanner, UserDao userDao,
                     ActivityLogDao activityLogDao) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.activityLogDao = activityLogDao;
    }

    public void show(User admin) {
        boolean loggedIn = true;

        while (loggedIn) {
            ConsoleUi.printHeader("ADMIN MENU");
            System.out.println("Logged in as: " + admin.getUsername());
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role and Status");
            System.out.println("5. View System Audit Logs");
            System.out.println("6. Log Out");
            System.out.println("----------------------------------");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewAllUsers();
                    break;

                case "2":
                    viewPendingUsers();
                    break;

                case "3":
                    approveUserAccount(admin);
                    break;

                case "4":
                    updateUserRoleAndStatus(admin);
                    break;

                case "5":
                    showAuditLogMenu(admin);
                    break;

                case "6":
                    loggedIn = false;
                    System.out.println(
                            "You have logged out successfully."
                    );
                    break;

                default:
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }
    }

    private void viewAllUsers() {
        System.out.println("\n--- ALL SYSTEM USERS ---");

        try {
            List<User> users = userDao.getAllUsers();

            if (users.isEmpty()) {
                System.out.println("No registered users found.");
                return;
            }

            printUserTable(users);

        } catch (SQLException e) {
            System.out.println(
                    "Unable to load users: " + e.getMessage()
            );
        }
    }

    private void viewPendingUsers() {
        System.out.println("\n--- PENDING REGISTRATIONS ---");

        try {
            List<User> pending = userDao.getPendingUsers();

            if (pending.isEmpty()) {
                System.out.println("No pending registrations.");
                return;
            }

            printUserTable(pending);

        } catch (SQLException e) {
            System.out.println(
                    "Unable to load pending users: " + e.getMessage()
            );
        }
    }

    private void approveUserAccount(User admin) {
        System.out.println("\n--- APPROVE USER ACCOUNT ---");

        try {
            List<User> pending = userDao.getPendingUsers();

            if (pending.isEmpty()) {
                System.out.println("No pending registrations.");
                return;
            }

            printUserTable(pending);

            while (true) {
                int userId = readIntInput(
                        "Enter User ID to approve (0 to cancel): "
                );

                if (userId == CANCELLED) {
                    System.out.println("Approval cancelled.");
                    return;
                }

                User target = findById(pending, userId);

                if (target == null) {
                    System.out.println(
                            "User ID " + userId
                                    + " is not in the pending list."
                    );
                    continue;
                }

                if (userDao.approveUser(admin.getUserId(), userId)) {
                    System.out.println(
                            "User '" + target.getUsername()
                                    + "' (ID " + userId
                                    + ") has been APPROVED."
                    );
                } else {
                    System.out.println(
                            "Approval failed. The account may "
                                    + "no longer be pending."
                    );
                }
                return;
            }

        } catch (SQLException e) {
            System.out.println(
                    "Approval failed: " + e.getMessage()
            );
        }
    }

    private void updateUserRoleAndStatus(User currentAdmin) {
        System.out.println("\n--- UPDATE USER ROLE AND STATUS ---");

        try {
            List<User> users = userDao.getAllUsers();

            if (users.isEmpty()) {
                System.out.println("No registered users found.");
                return;
            }

            printUserTable(users);

            // Step 1: choose target user
            User target;
            while (true) {
                int targetUserId = readIntInput(
                        "Enter User ID to update (0 to cancel): "
                );

                if (targetUserId == CANCELLED) {
                    System.out.println("Update cancelled.");
                    return;
                }

                if (targetUserId == currentAdmin.getUserId()) {
                    System.out.println(
                            "WARNING: Modifying the currently active "
                                    + "admin account is restricted."
                    );
                    continue;
                }

                target = findById(users, targetUserId);

                if (target == null) {
                    System.out.println(
                            "User ID " + targetUserId
                                    + " does not exist."
                    );
                    continue;
                }
                break;
            }

            // Step 2: new role
            String newRole = readValidRole();
            if (newRole == null) {
                System.out.println("Update cancelled.");
                return;
            }

            // Step 3: new status
            String newStatus = readValidStatus();
            if (newStatus == null) {
                System.out.println("Update cancelled.");
                return;
            }

            if (userDao.updateUserRoleAndStatus(
                    currentAdmin.getUserId(), target.getUserId(),
                    newRole, newStatus)) {
                System.out.println(
                        "User '" + target.getUsername()
                                + "' (ID " + target.getUserId()
                                + ") updated successfully: role = "
                                + newRole + ", status = "
                                + newStatus + "."
                );
            } else {
                System.out.println(
                        "Update failed. The user was not found."
                );
            }

        } catch (SQLException e) {
            System.out.println(
                    "Update failed: " + e.getMessage()
            );
        }
    }

    // ---------- Audit log interface ----------

    private void showAuditLogMenu(User admin) {
        boolean back = false;

        while (!back) {
            System.out.println("\n--- SYSTEM AUDIT LOGS ---");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear All Logs");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewAllLogs();
                    break;

                case "2":
                    viewLogsByUser();
                    break;

                case "3":
                    clearLogs(admin);
                    break;

                case "0":
                    back = true;
                    break;

                default:
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }
    }

    private void viewAllLogs() {
        System.out.println("\n--- ALL SYSTEM LOGS ---");

        try {
            List<ActivityLog> logs = activityLogDao.getAllLogs();

            if (logs.isEmpty()) {
                System.out.println("No activity logs recorded.");
                return;
            }

            printLogTable(logs);

        } catch (SQLException e) {
            System.out.println(
                    "Unable to load logs: " + e.getMessage()
            );
        }
    }

    private void viewLogsByUser() {
        System.out.println("\n--- FILTER LOGS BY USER ID ---");

        int userId = readIntInput(
                "Enter User ID to filter (0 to cancel): "
        );

        if (userId == CANCELLED) {
            System.out.println("Filter cancelled.");
            return;
        }

        try {
            List<ActivityLog> logs =
                    activityLogDao.getLogsByUserId(userId);

            if (logs.isEmpty()) {
                System.out.println(
                        "No logs found for User ID " + userId + "."
                );
                return;
            }

            printLogTable(logs);

        } catch (SQLException e) {
            System.out.println(
                    "Unable to load logs: " + e.getMessage()
            );
        }
    }

    // Requires typing YES, and the clearing itself is logged afterwards.
    private void clearLogs(User admin) {
        System.out.println("\n--- CLEAR ALL LOGS ---");
        System.out.println(
                "WARNING: This permanently deletes every log entry."
        );
        System.out.print("Type YES to confirm (anything else cancels): ");

        String confirm = scanner.nextLine().trim();

        if (!confirm.equals("YES")) {
            System.out.println("Clear cancelled. No logs were deleted.");
            return;
        }

        try {
            int removed = activityLogDao.deleteAllLogs();

            System.out.println(removed + " log entries deleted.");

            activityLogDao.logAction(admin.getUserId(), "LOGS_CLEARED",
                    "Admin " + admin.getUserId() + " cleared "
                            + removed + " log entries");

        } catch (SQLException e) {
            System.out.println(
                    "Unable to clear logs: " + e.getMessage()
            );
        }
    }

    private void printLogTable(List<ActivityLog> logs) {
        String[] headers = {
                "ID", "User ID", "Action Type", "Description", "Timestamp"
        };
        int[] widths = {5, 8, 18, 35, 20};

        List<String[]> rows = new ArrayList<>();
        for (ActivityLog log : logs) {
            rows.add(new String[]{
                    String.valueOf(log.getId()),
                    String.valueOf(log.getUserId()),
                    log.getActionType(),
                    log.getDescription(),
                    log.getTimestamp()
            });
        }

        TablePrinter.print(headers, widths, rows);
        System.out.println(logs.size() + " log entries shown.");
    }

    // ---------- Input validation wrappers ----------

    // Keeps asking until the input is a whole number
    private int readIntInput(String prompt) {
        while (true) {
            System.out.println();
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid input. Please enter a number."
                );
            }
        }
    }

    // Returns a valid role, or null if the admin entered 0 to cancel
    private String readValidRole() {
        return ConsoleUi.chooseOption(scanner, "Valid roles:", VALID_ROLES,
                "Enter new role (0 to cancel): ",
                "Invalid role. Please try again.");
    }

    // Returns a valid status, or null if the admin entered 0 to cancel
    private String readValidStatus() {
        return ConsoleUi.chooseOption(scanner, "Valid statuses:",
                VALID_STATUSES, "Enter new status (0 to cancel): ",
                "Invalid status. Please try again.");
    }

    // ---------- Helpers ----------

    private User findById(List<User> users, int userId) {
        for (User user : users) {
            if (user.getUserId() == userId) {
                return user;
            }
        }
        return null;
    }

    // Reusable table renderer for any list of users
    private void printUserTable(List<User> users) {
        String[] headers = {"ID", "Username", "Role", "Status"};
        int[] widths = {5, 20, 15, 10};

        List<String[]> rows = new ArrayList<>();
        for (User user : users) {
            rows.add(new String[]{
                    String.valueOf(user.getUserId()),
                    user.getUsername(),
                    user.getRole(),
                    user.getStatus()
            });
        }

        TablePrinter.print(headers, widths, rows);
    }
}