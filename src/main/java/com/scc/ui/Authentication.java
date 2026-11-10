package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class Authentication {

    // TODO: match these to the roles in Section 2 of your System Proposal
    private static final String[] ROLES = {
            "ADMIN", "CUSTOMER", "METER_READER", "CASHIER"
    };

    private final Scanner scanner;
    private final UserDao userDao;
    private final MenuScreens menuScreens;

    public Authentication(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.menuScreens = new MenuScreens(scanner, userDao);
    }

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n==================================");
            System.out.println("       WATER BILLING SYSTEM");
            System.out.println("==================================");
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

                routeByRole(user);

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
            case "METER_READER":
                menuScreens.showMeterReaderMenu(user);
                break;
            case "CASHIER":
                menuScreens.showCashierMenu(user);
                break;
            default:
                System.out.println("Unknown role: " + user.getRole());
        }
    }
}