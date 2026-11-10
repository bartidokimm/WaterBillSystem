package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private static final String ROW_FORMAT =
            "| %-5s | %-15s | %-15s | %-10s |%n";

    private static final String[] VALID_ROLES = {
            "ADMIN", "CUSTOMER", "BILLING_STAFF", "CASHIER"
    };

    private static final String[] VALID_STATUSES = {
            "APPROVED", "PENDING"
    };

    private static final int CANCELLED = 0;

    private final Scanner scanner;
    private final UserDao userDao;

    public AdminMenu(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void show(User admin) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n==================================");
            System.out.println("            ADMIN MENU");
            System.out.println("==================================");
            System.out.println("Logged in as: " + admin.getUsername());
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role and Status");
            System.out.println("5. Log Out");
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
                    approveUserAccount();
                    break;

                case "4":
                    updateUserRoleAndStatus(admin);
                    break;

                case "5":
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

    private void approveUserAccount() {
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

                if (userDao.approveUser(userId)) {
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
                    target.getUserId(), newRole, newStatus)) {
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

    // ---------- Input validation wrappers ----------

    // Keeps asking until the input is a whole number
    private int readIntInput(String prompt) {
        while (true) {
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
        while (true) {
            System.out.println("\nValid roles: "
                    + String.join(", ", VALID_ROLES));
            System.out.print("Enter new role (0 to cancel): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("0")) {
                return null;
            }

            for (String role : VALID_ROLES) {
                if (role.equals(input)) {
                    return role;
                }
            }

            System.out.println("Invalid role. Please try again.");
        }
    }

    // Returns a valid status, or null if the admin entered 0 to cancel
    private String readValidStatus() {
        while (true) {
            System.out.println("\nValid statuses: "
                    + String.join(", ", VALID_STATUSES));
            System.out.print("Enter new status (0 to cancel): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("0")) {
                return null;
            }

            for (String status : VALID_STATUSES) {
                if (status.equals(input)) {
                    return status;
                }
            }

            System.out.println("Invalid status. Please try again.");
        }
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
        String border = "+" + "-".repeat(7)
                + "+" + "-".repeat(17)
                + "+" + "-".repeat(17)
                + "+" + "-".repeat(12) + "+";

        System.out.println(border);
        System.out.printf(ROW_FORMAT,
                "ID", "Username", "Role", "Status");
        System.out.println(border);

        for (User user : users) {
            System.out.printf(ROW_FORMAT,
                    user.getUserId(),
                    user.getUsername(),
                    user.getRole(),
                    user.getStatus());
        }

        System.out.println(border);
    }
}