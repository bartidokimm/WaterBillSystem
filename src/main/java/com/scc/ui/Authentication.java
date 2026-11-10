package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class Authentication {

    // Roles from Section 2 of the System Proposal:
    // Admin / Manager, Customer, Billing Staff, Cashier
    private static final String[] ROLES = {
            "ADMIN", "CUSTOMER", "BILLING_STAFF", "CASHIER"
    };

    private final Scanner scanner;
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final MenuScreens menuScreens;

    public Authentication(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.activityLogDao = new ActivityLogDao();
        this.menuScreens =
                new MenuScreens(scanner, userDao, activityLogDao);
    }

    public void start() {
        boolean running = true;

        while (running) {
            ConsoleUi.printHeader("WATER BILLING SYSTEM");
            System.out.println("1. Register Account");
            System.out.println("2. Log In");
            System.out.println("3. Exit");
            System.out.println("----------------------------------");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    registerAccount();
                    break;

                case "2":
                    logIn();
                    break;

                case "3":
                    running = false;
                    System.out.println(
                            "Thank you for using Water Billing System!"
                    );
                    break;

                default:
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }
    }

    private void registerAccount() {
        System.out.println("\n--- REGISTER ACCOUNT ---");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        String password = readPassword("Enter password: ");

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println(
                    "Username and password are required."
            );
            return;
        }

        String role = selectRole();

        try {
            boolean success = userDao.register(username, password, role);

            if (success) {
                System.out.println(
                        "Account registered successfully!"
                );
                System.out.println("Account Status: PENDING");
                System.out.println(
                        "Please wait for admin approval."
                );
            } else {
                System.out.println(
                        "Username already exists or input is invalid."
                );
            }

        } catch (SQLException e) {
            System.out.println(
                    "Registration failed: " + e.getMessage()
            );
        }
    }

    private void logIn() {
        System.out.println("\n--- LOG IN ---");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        String password = readPassword("Enter password: ");

        try {
            User user = userDao.login(username, password);

            if (user != null) {
                System.out.println("\nLogin successful!");
                System.out.println(
                        "Welcome, " + user.getUsername() + "!"
                );

                activityLogDao.logAction(user.getUserId(), "USER_LOGIN",
                        "User " + user.getUsername() + " (ID: "
                                + user.getUserId() + ") logged in.");

                routeByRole(user);

                logout(user);

            } else {
                System.out.println(
                        "Invalid username/password, "
                                + "or account is pending approval."
                );
            }

        } catch (SQLException e) {
            System.out.println(
                    "Login failed: " + e.getMessage()
            );
        }
    }

    // Runs after a role menu returns (the user chose Log Out).
    private void logout(User user) {
        activityLogDao.logAction(user.getUserId(), "USER_LOGOUT",
                "User " + user.getUsername() + " (ID: "
                        + user.getUserId() + ") logged out.");
    }

    // Automatically display the password after typing
    private String readPassword(String prompt) {
        System.out.print(prompt);

        String password = scanner.nextLine();

        System.out.println("Password entered: " + password);

        return password;
    }

    private String selectRole() {
        while (true) {
            System.out.println("\nSelect role:");
            for (int i = 0; i < ROLES.length; i++) {
                System.out.println((i + 1) + ". " + ROLES[i]);
            }
            System.out.print("Enter choice: ");

            String input = scanner.nextLine().trim();

            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < ROLES.length) {
                    return ROLES[index];
                }
            } catch (NumberFormatException e) {
                // fall through to invalid message
            }

            System.out.println("Invalid role choice. Please try again.");
        }
    }

    private void routeByRole(User user) {
        switch (user.getRole()) {
            case "ADMIN":
                menuScreens.showAdminMenu(user);
                break;
            case "CUSTOMER":
                menuScreens.showCustomerMenu(user);
                break;
            case "BILLING_STAFF":
                menuScreens.showBillingStaffMenu(user);
                break;
            case "CASHIER":
                menuScreens.showCashierMenu(user);
                break;
            default:
                System.out.println("Unknown role: " + user.getRole());
        }
    }
}