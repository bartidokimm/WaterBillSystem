package com.scc;

import com.scc.dao.DatabaseConnection;
import com.scc.dao.UserDao;
import com.scc.ui.MenuScreens;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try  {
            DatabaseConnection.initializeDatabase();

            Scanner scanner = new Scanner(System.in);
            UserDao userDao = new UserDao();

            MenuScreens menu =
                    new MenuScreens(scanner, userDao);

            menu.showMainMenu();

            scanner.close();

        } catch (SQLException e) {
            System.out.println(
                    "Unable to connect to the database."
            );
            System.out.println(
                    "Error: " + e.getMessage()
            );
        }
    }
}