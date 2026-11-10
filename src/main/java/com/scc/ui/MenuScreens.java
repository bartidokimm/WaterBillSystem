package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.BillDao;
import com.scc.dao.CustomerDao;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class MenuScreens {

    private final Scanner scanner;
    private final ActivityLogDao activityLogDao;
    private final AdminMenu adminMenu;
    private final BillingStaffMenu billingStaffMenu;

    public MenuScreens(Scanner scanner, UserDao userDao,
                       ActivityLogDao activityLogDao) {
        this.scanner = scanner;
        this.activityLogDao = activityLogDao;
        this.adminMenu = new AdminMenu(scanner, userDao, activityLogDao);
        this.billingStaffMenu = new BillingStaffMenu(
                scanner, new CustomerDao(), new BillDao());
    }

    public void showAdminMenu(User user) {
        adminMenu.show(user);
    }

    public void showCustomerMenu(User user) {
        showUserMenu(user);
    }

    public void showBillingStaffMenu(User user) {
        billingStaffMenu.show(user);
    }

    public void showCashierMenu(User user) {
        showUserMenu(user);
    }

    public void showUserMenu(User user) {
        boolean loggedIn = true;

        while (loggedIn) {
            ConsoleUi.printHeader("USER MENU");
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
                    // Exit ends the program, so log the logout here.
                    activityLogDao.logAction(user.getUserId(),
                            "USER_LOGOUT",
                            "User " + user.getUsername() + " (ID: "
                                    + user.getUserId() + ") logged out.");
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