
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class MenuScreens {

    private final Scanner scanner;
    private final UserDao userDao;

    public MenuScreens(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void showMainMenu() {
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

        try {
            boolean success = userDao.register(username, password);

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

                showUserMenu(user);

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

    private void showUserMenu(User user) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n==================================");
            System.out.println("          USER MENU");
            System.out.println("==================================");
            System.out.println(
                    "Logged in as: " + user.getUsername()
            );
            System.out.println("1. Log Out");
            System.out.println("2. Exit");
            System.out.println("----------------------------------");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    loggedIn = false;
                    System.out.println(
                            "You have logged out successfully."
                    );
                    break;

                case "2":
                    System.out.println(
                            "Thank you for using the system!"
                    );
                    System.exit(0);
                    break;

                default:
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }
    }
}