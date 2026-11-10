package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try  {
            DatabaseInitializer.initialize();

            Scanner scanner = new Scanner(System.in);
            UserDao userDao = new UserDao();

            Authentication authentication =
                    new Authentication(scanner, userDao);

            authentication.start();

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