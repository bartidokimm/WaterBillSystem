package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private static final String ROW_FORMAT =
            "| %-5s | %-15s | %-15s | %-10s |%n";

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
            System.out.println("4. Log Out");
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
                System.out.print(
                        "Enter User ID to approve (0 to cancel): "
                );

                String input = scanner.nextLine().trim();

                int userId;
                try {
                    userId = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println(
                            "Invalid input. Please enter a number."
                    );
                    continue;
                }

                if (userId == 0) {
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