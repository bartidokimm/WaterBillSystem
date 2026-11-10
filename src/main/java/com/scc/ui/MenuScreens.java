package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class MenuScreens {

    private final Scanner scanner;
    private final UserDao userDao;
    private final AdminMenu adminMenu;

    public MenuScreens(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.adminMenu = new AdminMenu(scanner, userDao);
    }

    public void showAdminMenu(User user) {
        adminMenu.show(user);
    }

    public void showCustomerMenu(User user) {
        showUserMenu(user);
    }

    public void showBillingStaffMenu(User user) {
        showUserMenu(user);
    }

    public void showCashierMenu(User user) {
        showUserMenu(user);
    }

    public void showUserMenu(User user) {
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